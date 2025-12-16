package pl.su.su_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.BaseIntegrationTest;
import pl.su.su_backend.dto.budget.CouncilBudgetRequestDto;
import pl.su.su_backend.dto.budget.CouncilTransactionRequestDto;
import pl.su.su_backend.model.budget.CouncilBudget;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.TransactionType;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.budget.CouncilBudgetRepository;
import pl.su.su_backend.repositories.budget.CouncilTransactionRepository;
import pl.su.su_backend.repositories.council.CouncilMemberRepository;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.JwtService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class CouncilBudgetControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CouncilBudgetRepository budgetRepository;

    @Autowired
    private CouncilTransactionRepository transactionRepository;

    @Autowired
    private CouncilRepository councilRepository;

    @Autowired
    private CouncilMemberRepository memberRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EntityManager entityManager;

    private final String SUPERVISOR_EMAIL = "nauczyciel@school.edu";
    private final String TREASURER_EMAIL = "student3@school.edu";
    private final String STUDENT_EMAIL = "student6@school.edu";
    private final String COUNCIL_CODE = "SU2025";



    @Test
    void shouldCreateBudgetSuccessfullyBySupervisor() throws Exception {
        Cookie authCookie = generateAuthCookie(SUPERVISOR_EMAIL, "Opiekun");
        UUID councilId = getCouncilIdByCode(COUNCIL_CODE);
        BigDecimal initialAmount = new BigDecimal("5000.00");
        String newYear = "2030/2031";

        CouncilBudgetRequestDto requestDto = CouncilBudgetRequestDto.builder()
                .councilId(councilId)
                .year(newYear)
                .initialAmount(initialAmount)
                .build();

        mockMvc.perform(post("/api/councils/{councilId}/budget", councilId)
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(newYear))
                .andExpect(jsonPath("$.initialAmount").value(5000.00))
                .andExpect(jsonPath("$.balance").value(5000.00));

        boolean exists = budgetRepository.findByCouncil_IdAndYear(councilId, newYear).isPresent();
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnConflictWhenCreatingDuplicateBudget() throws Exception {
        Cookie authCookie = generateAuthCookie(SUPERVISOR_EMAIL, "Opiekun");
        UUID councilId = getCouncilIdByCode(COUNCIL_CODE);
        String existingYear = "2025/2026";

        CouncilBudgetRequestDto requestDto = CouncilBudgetRequestDto.builder()
                .councilId(councilId)
                .year(existingYear)
                .initialAmount(new BigDecimal("1000.00"))
                .build();

        mockMvc.perform(post("/api/councils/{councilId}/budget", councilId)
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Budżet na rok " + existingYear + " już istnieje"));
    }

    @Test
    void shouldAllowCreatingBudgetsForSameYearInDifferentCouncils() throws Exception {
        Cookie authCookie = generateAuthCookie(SUPERVISOR_EMAIL, "Opiekun");
        UUID councilId1 = getCouncilIdByCode("SU2025");
        String commonYear = "2090/2091";

        Council council2 = Council.builder()
                .name("New Test Council")
                .academicYear("2090/2091")
                .startDate(LocalDate.of(2090, 9, 1))
                .endDate(LocalDate.of(2091, 6, 30))
                .active(true)
                .defaultCouncil(false)
                .joinCode("NEW_SU_TEST")
                .build();
        councilRepository.save(council2);
        UUID councilId2 = council2.getId();

        addMemberToCouncil(councilId2, SUPERVISOR_EMAIL, RoleCode.OPIEKUN_SU);

        CouncilBudgetRequestDto dto1 = CouncilBudgetRequestDto.builder()
                .councilId(councilId1)
                .year(commonYear)
                .initialAmount(new BigDecimal("1000.00"))
                .build();

        mockMvc.perform(post("/api/councils/{councilId}/budget", councilId1)
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isOk());

        CouncilBudgetRequestDto dto2 = CouncilBudgetRequestDto.builder()
                .councilId(councilId2)
                .year(commonYear)
                .initialAmount(new BigDecimal("5000.00"))
                .build();

        mockMvc.perform(post("/api/councils/{councilId}/budget", councilId2)
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isOk());

        long count = budgetRepository.findAll().stream()
                .filter(b -> b.getYear().equals(commonYear))
                .count();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldForbidStudentFromCreatingBudget() throws Exception {
        Cookie authCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");
        UUID councilId = getCouncilIdByCode(COUNCIL_CODE);

        CouncilBudgetRequestDto requestDto = CouncilBudgetRequestDto.builder()
                .councilId(councilId)
                .year("2040/2041")
                .initialAmount(BigDecimal.TEN)
                .build();

        mockMvc.perform(post("/api/councils/{councilId}/budget", councilId)
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }


    @Test
    void shouldAddExpenseAndUpdateBalanceSuccessfully() throws Exception {
        Cookie authCookie = generateAuthCookie(TREASURER_EMAIL, "Skarbnik");
        UUID councilId = getCouncilIdByCode(COUNCIL_CODE);
        CouncilBudget budget = budgetRepository.findByCouncil_IdAndYear(councilId, "2025/2026")
                .orElseThrow(() -> new RuntimeException("Brak budżetu testowego"));

        BigDecimal initialBalance = budget.getBalance();
        BigDecimal expenseAmount = new BigDecimal("450.00");

        CouncilTransactionRequestDto requestDto = CouncilTransactionRequestDto.builder()
                .budgetId(budget.getId())
                .type(TransactionType.EXPENSE)
                .amount(expenseAmount)
                .description("Test expense")
                .date(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/councils/budget/{budgetId}/transactions", budget.getId())
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(450.00));

        entityManager.flush();
        entityManager.clear();

        CouncilBudget updatedBudget = budgetRepository.findById(budget.getId()).orElseThrow();
        BigDecimal expectedBalance = initialBalance.subtract(expenseAmount);

        assertThat(updatedBudget.getBalance()).isEqualByComparingTo(expectedBalance);
    }

    @Test
    void shouldAddIncomeAndUpdateBalance() throws Exception {
        Cookie authCookie = generateAuthCookie(TREASURER_EMAIL, "Skarbnik");
        UUID councilId = getCouncilIdByCode(COUNCIL_CODE);
        CouncilBudget budget = budgetRepository.findByCouncil_IdAndYear(councilId, "2025/2026").orElseThrow();
        BigDecimal initialBalance = budget.getBalance();

        CouncilTransactionRequestDto requestDto = CouncilTransactionRequestDto.builder()
                .budgetId(budget.getId())
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100.00"))
                .description("Test Income")
                .date(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/councils/budget/{budgetId}/transactions", budget.getId())
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();
        CouncilBudget updatedBudget = budgetRepository.findById(budget.getId()).orElseThrow();

        assertThat(updatedBudget.getBalance()).isEqualByComparingTo(initialBalance.add(new BigDecimal("100.00")));
    }

    @Test
    void shouldForbidStudentFromAddingTransaction() throws Exception {
        Cookie authCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");
        UUID councilId = getCouncilIdByCode(COUNCIL_CODE);
        CouncilBudget budget = budgetRepository.findByCouncil_IdAndYear(councilId, "2025/2026").orElseThrow();

        CouncilTransactionRequestDto requestDto = CouncilTransactionRequestDto.builder()
                .budgetId(budget.getId())
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("50.00"))
                .description("Test Permissions")
                .date(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/councils/budget/{budgetId}/transactions", budget.getId())
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldPreventAddingTransactionToInactiveCouncilBudget() throws Exception {
        Cookie authCookie = generateAuthCookie(TREASURER_EMAIL, "Skarbnik");
        UUID oldCouncilId = getCouncilIdByCode("OLD2023");
        CouncilBudget oldBudget = budgetRepository.findByCouncil_IdAndYear(oldCouncilId, "2023/2024")
                .orElseThrow(() -> new RuntimeException("Brak starego budżetu w bazie testowej"));

        CouncilTransactionRequestDto requestDto = CouncilTransactionRequestDto.builder()
                .budgetId(oldBudget.getId())
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("100.00"))
                .description("TEst")
                .date(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/councils/budget/{budgetId}/transactions", oldBudget.getId())
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Samorząd nie jest aktywny. Nie można dodać transakcji!"));
    }


    private Cookie generateAuthCookie(String email, String fullName) {
        String token = jwtService.generateToken(email, fullName);
        return new Cookie("accessToken", token);
    }

    private UUID getCouncilIdByCode(String code) {
        return councilRepository.findByJoinCode(code)
                .orElseThrow(() -> new RuntimeException("Council not found: " + code))
                .getId();
    }

    private void addMemberToCouncil(UUID councilId, String userEmail, RoleCode roleCode) {
        Users user = usersRepository.findByEmail(userEmail).orElseThrow();
        Council council = councilRepository.findById(councilId).orElseThrow();

        CouncilMember.CouncilMemberId memberId =
                new CouncilMember.CouncilMemberId(councilId, user.getId());

        CouncilMember member = CouncilMember.builder()
                .id(memberId)
                .council(council)
                .user(user)
                .role(roleCode)
                .build();

        memberRepository.save(member);
    }
}