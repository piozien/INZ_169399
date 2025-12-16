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
import pl.su.su_backend.dto.suggestion.SuggestionRequestDto;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.enums.SuggestionStatus;
import pl.su.su_backend.model.suggestion.Suggestion;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.repositories.suggestion.SuggestionRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.JwtService;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class SuggestionControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Autowired
    private CouncilRepository councilRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JwtService jwtService;

    private final String DIRECTOR_EMAIL = "dyrektor@school.edu";
    private final String SUPERVISOR_EMAIL = "nauczyciel@school.edu";
    private final String CHAIRMAN_EMAIL = "student1@school.edu";
    private final String STUDENT_EMAIL = "student6@school.edu";
    private final String COUNCIL_CODE = "SU2025";

    @Test
    void shouldCreateSuggestionSuccessfullyWithExplicitCouncil() throws Exception {
        Cookie authCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");
        UUID councilId = getCouncilIdByCode(COUNCIL_CODE);
        UUID userId = getUserIdByEmail(STUDENT_EMAIL);

        SuggestionRequestDto requestDto = SuggestionRequestDto.builder()
                .userId(userId)
                .title("Test Council")
                .description("Description")
                .councilId(councilId)
                .anonymous(false)
                .tags(Set.of("Test"))
                .build();

        mockMvc.perform(post("/api/suggestions")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.councilId").value(councilId.toString()));
    }

    @Test
    void shouldCreateSuggestionAssigningDefaultCouncil() throws Exception {
        Cookie authCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");
        UUID userId = getUserIdByEmail(STUDENT_EMAIL);
        UUID defaultCouncilId = getCouncilIdByCode(COUNCIL_CODE);

        SuggestionRequestDto requestDto = SuggestionRequestDto.builder()
                .userId(userId)
                .title("Default Council")
                .description("Request without councilid")
                .councilId(null)
                .anonymous(true)
                .build();

        mockMvc.perform(post("/api/suggestions")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Default Council"))
                .andExpect(jsonPath("$.councilId").value(defaultCouncilId.toString()));
    }

    @Test
    void shouldFailToCreateSuggestionWithoutTitle() throws Exception {
        Cookie authCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");
        UUID userId = getUserIdByEmail(STUDENT_EMAIL);

        SuggestionRequestDto requestDto = SuggestionRequestDto.builder()
                .userId(userId)
                .title("")
                .description("Opis")
                .build();

        mockMvc.perform(post("/api/suggestions")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldApproveSuggestionBySupervisor() throws Exception {
        Suggestion suggestion = createSuggestionInDb("Test", STUDENT_EMAIL);
        Cookie supervisorCookie = generateAuthCookie(SUPERVISOR_EMAIL, "Opiekun");

        mockMvc.perform(put("/api/suggestions/{id}/approve", suggestion.getId())
                        .cookie(supervisorCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        Suggestion updated = suggestionRepository.findById(suggestion.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SuggestionStatus.APPROVED);
    }

    @Test
    void shouldRejectSuggestionByChairman() throws Exception {
        Suggestion suggestion = createSuggestionInDb("Test", STUDENT_EMAIL);
        Cookie chairmanCookie = generateAuthCookie(CHAIRMAN_EMAIL, "Przewodniczacy");
        String reason = "Test reason";

        mockMvc.perform(put("/api/suggestions/{id}/reject", suggestion.getId())
                        .cookie(chairmanCookie)
                        .param("rejectionReason", reason))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void shouldForbidStudentFromApprovingSuggestion() throws Exception {
        Suggestion suggestion = createSuggestionInDb("Test permissions", STUDENT_EMAIL);
        Cookie studentCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");

        mockMvc.perform(put("/api/suggestions/{id}/approve", suggestion.getId())
                        .cookie(studentCookie))
                .andExpect(status().isForbidden());

        Suggestion unchanged = suggestionRepository.findById(suggestion.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(SuggestionStatus.PENDING);
    }

    @Test
    void shouldAllowDirectorToDeleteSuggestion() throws Exception {
        Suggestion suggestion = createSuggestionInDb("Spam", STUDENT_EMAIL);
        Cookie directorCookie = generateAuthCookie(DIRECTOR_EMAIL, "Dyrektor");

        mockMvc.perform(delete("/api/suggestions/{id}", suggestion.getId())
                        .cookie(directorCookie))
                .andExpect(status().isNoContent());

        assertThat(suggestionRepository.findById(suggestion.getId())).isEmpty();
    }

    @Test
    void shouldForbidSupervisorFromDeletingSuggestion() throws Exception {
        Suggestion suggestion = createSuggestionInDb("Suggestion to delete", STUDENT_EMAIL);
        Cookie supervisorCookie = generateAuthCookie(SUPERVISOR_EMAIL, "Opiekun");

        mockMvc.perform(delete("/api/suggestions/{id}", suggestion.getId())
                        .cookie(supervisorCookie))
                .andExpect(status().isForbidden());

        assertThat(suggestionRepository.findById(suggestion.getId())).isPresent();
    }

    @Test
    void shouldAllowAuthorToDeleteOwnSuggestion() throws Exception {
        Suggestion suggestion = createSuggestionInDb("Test", STUDENT_EMAIL);
        Cookie authorCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");

        mockMvc.perform(delete("/api/suggestions/{id}", suggestion.getId())
                        .cookie(authorCookie))
                .andExpect(status().isNoContent());

        assertThat(suggestionRepository.findById(suggestion.getId())).isEmpty();
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

    private UUID getUserIdByEmail(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email))
                .getId();
    }

    private Suggestion createSuggestionInDb(String title, String authorEmail) {
        Users author = usersRepository.findByEmail(authorEmail).orElseThrow();
        Council council = councilRepository.findByJoinCode(COUNCIL_CODE).orElseThrow();

        Suggestion suggestion = Suggestion.builder()
                .title(title)
                .description("Auto generated desc")
                .user(author)
                .council(council)
                .status(SuggestionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .anonymous(false)
                .build();

        return suggestionRepository.save(suggestion);
    }
}