package pl.su.su_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.BaseIntegrationTest;
import pl.su.su_backend.dto.council.CouncilRequestDto;
import pl.su.su_backend.dto.council.CouncilResponseDto;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.repositories.council.CouncilMemberRepository;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.JwtService;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class CouncilControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CouncilRepository councilRepository;

    @Autowired
    private CouncilMemberRepository councilMemberRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EntityManager entityManager;

    private final String ADMIN_EMAIL = "admin@school.edu";
    private final String STUDENT_EMAIL = "student6@school.edu";
    private final String CHAIRMAN_EMAIL = "student1@school.edu";
    private final String TARGET_MEMBER_EMAIL = "student5@school.edu";


    @Test
    void shouldCreateCouncilSuccessfullyByAdmin() throws Exception {
        Cookie adminCookie = generateAuthCookie(ADMIN_EMAIL, "Admin");

        CouncilRequestDto requestDto = CouncilRequestDto.builder()
                .name("Test Council 2030")
                .academicYear("2030/2031")
                .startDate(LocalDate.of(2030, 9, 1))
                .endDate(LocalDate.of(2031, 6, 30))
                .build();

        MvcResult result = mockMvc.perform(post("/api/councils")
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Council 2030"))
                .andExpect(jsonPath("$.joinCode").exists())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        CouncilResponseDto responseDto = objectMapper.readValue(jsonResponse, CouncilResponseDto.class);
        String generatedJoinCode = responseDto.getJoinCode();

        assertThat(councilRepository.findByJoinCode(generatedJoinCode)).isPresent();
    }

    @Test
    void shouldForbidStudentFromCreatingCouncil() throws Exception {
        Cookie studentCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");
        String illegalCouncilName = "User Council";

        CouncilRequestDto requestDto = CouncilRequestDto.builder()
                .name(illegalCouncilName)
                .academicYear("2099/2100")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(5))
                .build();

        mockMvc.perform(post("/api/councils")
                        .cookie(studentCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        boolean exists = councilRepository.findAll().stream()
                .anyMatch(c -> c.getName().equals(illegalCouncilName));

        assertThat(exists).isFalse();
    }


    @Test
    void shouldJoinCouncilWithValidCode() throws Exception {
        String joinCode = "SU2025";
        Cookie studentCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");

        mockMvc.perform(post("/api/councils/join/{joinCode}", joinCode)
                        .cookie(studentCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.joinCode").value(joinCode));

        entityManager.flush();
        entityManager.clear();

        UUID councilId = getCouncilIdByCode(joinCode);
        UUID userId = getUserIdByEmail(STUDENT_EMAIL);

        assertThat(councilMemberRepository.findByCouncilIdAndUserId(councilId, userId)).isPresent();
    }

    @Test
    void shouldReturnNotFoundForInvalidJoinCode() throws Exception {
        Cookie studentCookie = generateAuthCookie(STUDENT_EMAIL, "Student");

        mockMvc.perform(post("/api/councils/join/{joinCode}", "BAD_CODE_123")
                        .cookie(studentCookie))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictIfAlreadyMember() throws Exception {
        String joinCode = "SU2025";
        Cookie chairmanCookie = generateAuthCookie(CHAIRMAN_EMAIL, "Przewodniczacy");

        mockMvc.perform(post("/api/councils/join/{joinCode}", joinCode)
                        .cookie(chairmanCookie))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Użytkownik jest już członkiem tej rady."));
    }

    @Test
    void shouldAddMemberToCouncilByAuthorizedUser() throws Exception {
        Cookie adminCookie = generateAuthCookie(ADMIN_EMAIL, "Admin");
        UUID councilId = getCouncilIdByCode("SU2025");
        UUID newMemberId = getUserIdByEmail(STUDENT_EMAIL);

        mockMvc.perform(post("/api/councils/{councilId}/members", councilId)
                        .cookie(adminCookie)
                        .param("userId", newMemberId.toString())
                        .param("roleCode", RoleCode.SKARBNIK_SU.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SKARBNIK_SU"));

        entityManager.flush();
        entityManager.clear();

        var member = councilMemberRepository.findByCouncilIdAndUserId(councilId, newMemberId);
        assertThat(member).isPresent();
        assertThat(member.get().getRole()).isEqualTo(RoleCode.SKARBNIK_SU);
    }

    @Test
    void shouldForbidUnauthorizedUserFromRemovingMember() throws Exception {
        Cookie studentCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");
        UUID councilId = getCouncilIdByCode("SU2025");
        UUID targetMemberId = getUserIdByEmail(TARGET_MEMBER_EMAIL);

        mockMvc.perform(delete("/api/councils/{councilId}/members/{userId}", councilId, targetMemberId)
                        .cookie(studentCookie))
                .andExpect(status().isForbidden());

        boolean stillExists = councilMemberRepository.findByCouncilIdAndUserId(councilId, targetMemberId).isPresent();
        assertThat(stillExists).isTrue();
    }


    private Cookie generateAuthCookie(String email, String fullName) {
        String token = jwtService.generateToken(email, fullName);
        return new Cookie("accessToken", token);
    }

    private UUID getUserIdByEmail(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email))
                .getId();
    }

    private UUID getCouncilIdByCode(String code) {
        return councilRepository.findByJoinCode(code)
                .orElseThrow(() -> new RuntimeException("Council not found: " + code))
                .getId();
    }
}