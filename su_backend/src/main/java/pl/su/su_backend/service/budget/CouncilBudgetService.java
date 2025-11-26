package pl.su.su_backend.service.budget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.budget.*;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.budget.CouncilBudget;
import pl.su.su_backend.model.budget.CouncilTransaction;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.TransactionType;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.budget.CouncilBudgetRepository;
import pl.su.su_backend.repositories.budget.CouncilTransactionRepository;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.service.council.CouncilMemberService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.service.user.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CouncilBudgetService {

    private final CouncilBudgetRepository councilBudgetRepository;
    private final CouncilTransactionRepository councilTransactionRepository;
    private final CouncilRepository councilRepository;
    private final UserService userService;
    private final CouncilMemberService councilMemberService;
    private final ActivityLogService activityLogService;
    private final CouncilBudgetMapper budgetMapper;
    private final CouncilTransactionMapper transactionMapper;


    public CouncilBudgetResponseDto createBudget(UUID councilId, CouncilBudgetRequestDto dto, String currentUserEmail) {
        Users user = userService.getUserByEmailEntity(currentUserEmail);

        checkTreasurerOrHigher(user.getId(), councilId);

        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono samorządu"));

        String year = dto.getYear() != null ? dto.getYear() : String.valueOf(LocalDateTime.now().getYear());

        if (councilBudgetRepository.findByCouncil_IdAndYear(councilId, year).isPresent()) {
            throw ApiException.conflict("Budżet na rok " + year + " już istnieje");
        }

        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year(year)
                .initialAmount(dto.getInitialAmount())
                .balance(dto.getInitialAmount())
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .build();

        CouncilBudget saved = councilBudgetRepository.save(budget);

        activityLogService.log(user.getId(), ActionType.BUDGET_CREATE, "Utworzono budżet " + year);

        return budgetMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CouncilBudgetResponseDto getBudget(UUID councilId, String currentUserEmail) {
        Users user = userService.getUserByEmailEntity(currentUserEmail);
        if (!councilMemberService.isMemberOfCouncil(user.getId(), councilId)) {
            throw ApiException.forbidden("Brak dostępu do budżetu");
        }

        CouncilBudget budget = councilBudgetRepository.findByCouncil_IdOrderByYearDesc(councilId).stream()
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("Brak budżetu dla tego samorządu"));

        return budgetMapper.toResponse(budget);
    }


    public CouncilTransactionResponseDto addTransaction(UUID budgetId, CouncilTransactionRequestDto dto, String currentUserEmail) {
        Users user = userService.getUserByEmailEntity(currentUserEmail);
        CouncilBudget budget = councilBudgetRepository.findById(budgetId)
                .orElseThrow(() -> ApiException.notFound("Budżet nie istnieje"));

        checkTreasurerOrHigher(user.getId(), budget.getCouncil().getId());

        CouncilTransaction transaction = transactionMapper.toEntity(dto, budget, user);
        CouncilTransaction saved = councilTransactionRepository.save(transaction);

        updateBalance(budget);

        activityLogService.log(user.getId(), ActionType.TRANSACTION_CREATE,
                "Dodano transakcję: " + dto.getDescription());

        return transactionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CouncilTransactionResponseDto> getTransactions(UUID budgetId, String currentUserEmail) {
        Users user = userService.getUserByEmailEntity(currentUserEmail);
        CouncilBudget budget = councilBudgetRepository.findById(budgetId)
                .orElseThrow(() -> ApiException.notFound("Budżet nie istnieje"));

        if (!councilMemberService.isMemberOfCouncil(user.getId(), budget.getCouncil().getId())) {
            throw ApiException.forbidden("Brak dostępu");
        }

        return councilTransactionRepository.findByBudgetId(budgetId).stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }


    private void updateBalance(CouncilBudget budget) {
        BigDecimal income = councilTransactionRepository.findByBudgetId(budget.getId()).stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(CouncilTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expense = councilTransactionRepository.findByBudgetId(budget.getId()).stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(CouncilTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        budget.setBalance(budget.getInitialAmount().add(income).subtract(expense));
        councilBudgetRepository.save(budget);
    }

    private void checkTreasurerOrHigher(UUID userId, UUID councilId) {
        CouncilMember member = councilMemberService.getCouncilMembersInternal(councilId).stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> ApiException.forbidden("Nie jesteś członkiem tego samorządu"));

        RoleCode role = member.getRole();
        if (role != RoleCode.SKARBNIK_SU && role != RoleCode.PRZEWODNICZACY_SU && role != RoleCode.ZASTEPCA_SU && role != RoleCode.OPIEKUN_SU) {
            throw ApiException.forbidden("Tylko Skarbnik lub Zarząd może edytować budżet");
        }
    }
}