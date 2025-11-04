package pl.su.su_backend.controller.council;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import pl.su.su_backend.config.JwtConfig;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.testsupport.Fixtures;
import pl.su.su_backend.testsupport.OAuth2TestConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(OAuth2TestConfig.class)
class CouncilControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CouncilRepository councilRepository;

    @Autowired
    private JwtConfig jwtConfig;

    private Users testUser;
    private Users przewodniczacyUser;
    private Council testCouncil;
    private String validToken;
    private String przewodniczacyToken;

    @BeforeEach
    void setUp() {
        councilRepository.deleteAll();
        usersRepository.deleteAll();
        
        testUser = Fixtures.userWithStatusNoId("Test User", "test@test.com", StatusEnum.CONFIRMED);
        testUser.setAuthProvider(AuthProvider.LOCAL);
        testUser = usersRepository.save(testUser);

        Role uczenRole = roleRepository.findByRoleCode(RoleCode.UCZEN)
                .orElseThrow(() -> new RuntimeException("UCZEN role not found"));

        UserRole userRole = UserRole.builder()
                .user(testUser)
                .role(uczenRole)
                .assignedAt(LocalDateTime.now())
                .build();

        UserRole.Id userRoleId = new UserRole.Id();
        userRoleId.setUserId(testUser.getId());
        userRoleId.setRoleId(uczenRole.getId());
        userRole.setId(userRoleId);

        testUser.getUserRoles().add(userRole);
        testUser = usersRepository.save(testUser);

        przewodniczacyUser = Fixtures.userWithStatusNoId("Przewodniczacy User", "przewodniczacy@test.com", StatusEnum.CONFIRMED);
        przewodniczacyUser.setAuthProvider(AuthProvider.LOCAL);
        przewodniczacyUser = usersRepository.save(przewodniczacyUser);

        Role przewodniczacyRole = roleRepository.findByRoleCode(RoleCode.PRZEWODNICZACY_SU)
                .orElseThrow(() -> new RuntimeException("PRZEWODNICZACY_SU role not found"));

        UserRole przewodniczacyUserRole = UserRole.builder()
                .user(przewodniczacyUser)
                .role(przewodniczacyRole)
                .assignedAt(LocalDateTime.now())
                .build();

        UserRole.Id przewodniczacyUserRoleId = new UserRole.Id();
        przewodniczacyUserRoleId.setUserId(przewodniczacyUser.getId());
        przewodniczacyUserRoleId.setRoleId(przewodniczacyRole.getId());
        przewodniczacyUserRole.setId(przewodniczacyUserRoleId);

        przewodniczacyUser.getUserRoles().add(przewodniczacyUserRole);
        przewodniczacyUser = usersRepository.save(przewodniczacyUser);

        testCouncil = Fixtures.councilNoId("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        testCouncil.setJoinCode("SU20250001");
        testCouncil.setIsActive(true);
        testCouncil = councilRepository.save(testCouncil);

        validToken = jwtConfig.generateToken(testUser.getEmail());
        przewodniczacyToken = jwtConfig.generateToken(przewodniczacyUser.getEmail());
    }

    @Test
    void joinCouncilByCode_ShouldJoinSuccessfully_WhenValidCode() throws Exception {
        // Given
        Council joinTestCouncil = Fixtures.councilNoId("Join Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        joinTestCouncil.setJoinCode("SU20250002");
        joinTestCouncil.setIsActive(true);
        joinTestCouncil = councilRepository.save(joinTestCouncil);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity("/api/council/join/" + joinTestCouncil.getJoinCode(), 
                Fixtures.httpEntityWithToken(validToken), String.class);
        
        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("Join Test Council");
        assertThat(response.getBody()).contains("SU20250002");
    }

    @Test
    void joinCouncilByCode_ShouldReturnBadRequest_WhenInvalidCode() throws Exception {
        // When
        ResponseEntity<String> response = restTemplate.postForEntity("/api/council/join/INVALID_CODE", 
                Fixtures.httpEntityWithToken(validToken), String.class);
        
        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void joinCouncilByCode_ShouldReturnUnauthorized_WhenNoToken() throws Exception {
        // When
        ResponseEntity<String> response = restTemplate.postForEntity("/api/council/join/" + testCouncil.getJoinCode(), 
                Fixtures.httpEntityWithoutToken(""), String.class);
        
        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void joinCouncilByCode_ShouldReturnBadRequest_WhenUserAlreadyMember() throws Exception {
        // Given
        testCouncil.getMembers().add(testUser);
        testCouncil = councilRepository.save(testCouncil);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity("/api/council/join/" + testCouncil.getJoinCode(), 
                Fixtures.httpEntityWithToken(validToken), String.class);
        
        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).contains("already a member");
    }

    // PRZEWODNICZACY_SU
    @Test
    void createCouncil_ShouldReturnForbidden_WhenPrzewodniczacy() throws Exception {
        // Given 
        String councilJson = """
            {
                "name": "New Council",
                "academicYear": "2025/26",
                "startDate": "2025-01-01",
                "endDate": "2025-12-31"
            }
            """;

        // When
        ResponseEntity<String> response = restTemplate.postForEntity("/api/council",
                Fixtures.httpEntityWithToken(przewodniczacyToken, councilJson), String.class);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void getAllCouncils_ShouldReturnCouncilsList_WhenPrzewodniczacy() throws Exception {
        // Given
        testCouncil.getMembers().add(przewodniczacyUser);
        testCouncil = councilRepository.save(testCouncil);

        // When
        ResponseEntity<String> response = restTemplate.exchange("/api/council",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(przewodniczacyToken), String.class);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("Test Council");
    }

    @Test
    void getCouncilById_ShouldReturnCouncil_WhenPrzewodniczacy() throws Exception {
        // Given 
        testCouncil.getMembers().add(przewodniczacyUser);
        testCouncil = councilRepository.save(testCouncil);

        // When
        ResponseEntity<String> response = restTemplate.exchange("/api/council/" + testCouncil.getId(),
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(przewodniczacyToken), String.class);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("Test Council");
    }

    @Test
    void createBudget_ShouldCreateSuccessfully_WhenPrzewodniczacy() throws Exception {
        // Given - add przewodniczacy user to council as member
        testCouncil.getMembers().add(przewodniczacyUser);
        testCouncil = councilRepository.save(testCouncil);

        String budgetJson = """
            {
                "year": "2025",
                "initialAmount": 1000.00
            }
            """;

        // When
        ResponseEntity<String> response = restTemplate.postForEntity("/api/council/" + testCouncil.getId() + "/budget",
                Fixtures.httpEntityWithToken(przewodniczacyToken, budgetJson), String.class);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).contains("2025");
    }

    @Test
    void getBudget_ShouldReturnBudget_WhenPrzewodniczacy() throws Exception {
        // Given - add przewodniczacy user to council as member and create budget
        testCouncil.getMembers().add(przewodniczacyUser);
        testCouncil = councilRepository.save(testCouncil);

        // First create a budget
        String budgetJson = """
            {
                "year": "2025",
                "initialAmount": 1000.00
            }
            """;
        restTemplate.postForEntity("/api/council/" + testCouncil.getId() + "/budget",
                Fixtures.httpEntityWithToken(przewodniczacyToken, budgetJson), String.class);

        // When
        ResponseEntity<String> response = restTemplate.exchange("/api/council/" + testCouncil.getId() + "/budget",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(przewodniczacyToken), String.class);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("2025");
    }
}