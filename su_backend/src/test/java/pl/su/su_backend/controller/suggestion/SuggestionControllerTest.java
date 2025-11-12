package pl.su.su_backend.controller.suggestion;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import pl.su.su_backend.config.JwtConfig;
import pl.su.su_backend.dto.suggestion.SuggestionRequestDto;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.suggestion.SuggestionRepository;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.permission.PermissionRepository;
import pl.su.su_backend.repositories.user.UserRoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.testsupport.Fixtures;
import pl.su.su_backend.testsupport.OAuth2TestConfig;
import pl.su.su_backend.testsupport.RolePermissionTestHelper;
import pl.su.su_backend.model.roles.Role;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(OAuth2TestConfig.class)
class SuggestionControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Flyway flyway;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Autowired
    private JwtConfig jwtConfig;

    private Users authorUser;
    private Users reviewerUser;
    private String authorToken;
    private String reviewerToken;

    @BeforeEach
    void setUp() {
        flyway.migrate();

        userRoleRepository.deleteAll();
        suggestionRepository.deleteAll();
        usersRepository.deleteAll();

        Role authorRole = RolePermissionTestHelper.ensureRole(
                roleRepository, permissionRepository, RoleCode.UCZEN,
                PermissionCode.SUGGESTION_CREATE,
                PermissionCode.SUGGESTION_EDIT
        );

        Role reviewerRole = RolePermissionTestHelper.ensureRole(
                roleRepository, permissionRepository, RoleCode.OPIEKUN_SU,
                PermissionCode.SUGGESTION_VIEW,
                PermissionCode.SUGGESTION_APPROVE,
                PermissionCode.SUGGESTION_REJECT,
                PermissionCode.SUGGESTION_DELETE
        );

        authorUser = Fixtures.createUserWithRole(usersRepository, userRoleRepository,
                "Author", "author@test.local", StatusEnum.CONFIRMED, AuthProvider.LOCAL, authorRole);
        reviewerUser = Fixtures.createUserWithRole(usersRepository, userRoleRepository,
                "Reviewer", "reviewer@test.local", StatusEnum.CONFIRMED, AuthProvider.LOCAL, reviewerRole);

        authorToken = jwtConfig.generateToken(authorUser.getEmail());
        reviewerToken = jwtConfig.generateToken(reviewerUser.getEmail());
    }

    @Test
    void createSuggestion_ShouldReturnCreated_WhenAuthorHasPermission() {
        SuggestionRequestDto request = Fixtures.suggestionRequestDto(authorUser.getId(), "Idea", "Description", false);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/suggestions",
                Fixtures.httpEntityWithToken(authorToken, request),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("Idea");
    }

    @Test
    void getAllSuggestions_ShouldReturnOk_WhenReviewerHasPermission() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/suggestions",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(reviewerToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateSuggestion_ShouldReturnOk_WhenAuthorHasPermission() {
        SuggestionRequestDto create = Fixtures.suggestionRequestDto(authorUser.getId(), "Title", "Description", false);
        ResponseEntity<String> created = restTemplate.postForEntity(
                "/api/suggestions",
                Fixtures.httpEntityWithToken(authorToken, create),
                String.class
        );
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        UUID suggestionId = suggestionRepository.findAll().getFirst().getId();

        SuggestionRequestDto update = Fixtures.suggestionRequestDto(authorUser.getId(), "Updated title", "New description", false);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/suggestions/" + suggestionId,
                HttpMethod.PUT,
                Fixtures.httpEntityWithToken(authorToken, update),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Updated title");
    }

    @Test
    void approveSuggestion_ShouldReturnOk_WhenReviewerHasPermission() {
        SuggestionRequestDto create = Fixtures.suggestionRequestDto(authorUser.getId(), "Approve", "Description", false);
        ResponseEntity<String> created = restTemplate.postForEntity(
                "/api/suggestions",
                Fixtures.httpEntityWithToken(authorToken, create),
                String.class
        );
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        UUID suggestionId = suggestionRepository.findAll().getFirst().getId();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/suggestions/" + suggestionId + "/approve",
                HttpMethod.PUT,
                Fixtures.httpEntityWithToken(reviewerToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void rejectSuggestion_ShouldReturnOk_WhenReviewerHasPermission() {
        SuggestionRequestDto create = Fixtures.suggestionRequestDto(authorUser.getId(), "Reject", "Description", false);
        ResponseEntity<String> created = restTemplate.postForEntity(
                "/api/suggestions",
                Fixtures.httpEntityWithToken(authorToken, create),
                String.class
        );
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        UUID suggestionId = suggestionRepository.findAll().getFirst().getId();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/suggestions/" + suggestionId + "/reject?rejectionReason=No-budget",
                HttpMethod.PUT,
                Fixtures.httpEntityWithToken(reviewerToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteSuggestion_ShouldReturnNoContent_WhenReviewerHasPermission() {
        SuggestionRequestDto create = Fixtures.suggestionRequestDto(authorUser.getId(), "Remove", "Description", false);
        ResponseEntity<String> created = restTemplate.postForEntity(
                "/api/suggestions",
                Fixtures.httpEntityWithToken(authorToken, create),
                String.class
        );
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        UUID suggestionId = suggestionRepository.findAll().getFirst().getId();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/suggestions/" + suggestionId,
                HttpMethod.DELETE,
                Fixtures.httpEntityWithToken(reviewerToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}


