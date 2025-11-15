package pl.su.su_backend.controller.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import pl.su.su_backend.dto.user.*;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.repositories.user.UserRoleRepository;
import pl.su.su_backend.testsupport.Fixtures;
import pl.su.su_backend.testsupport.OAuth2TestConfig;

import static org.junit.jupiter.api.Assertions.*;
import static pl.su.su_backend.testsupport.CookieTestUtils.assertHasCookie;
import static pl.su.su_backend.testsupport.CookieTestUtils.extractCookieValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(OAuth2TestConfig.class)
public class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Users testUser;
    private Role uczenRole;
    private String testUserEmail;
    private String testUserPassword;

    @BeforeEach
    void setUp() throws Exception {
        userRoleRepository.deleteAll();
        usersRepository.deleteAll();
        roleRepository.deleteAll();

        testUserEmail = "test@example.com";
        testUserPassword = Fixtures.RAW_TEST_PASSWORD;

        uczenRole = Role.builder()
            .roleCode(RoleCode.UCZEN)
            .description("Uczeń")
            .build();
        uczenRole = roleRepository.save(uczenRole);

        testUser = Fixtures.userWithStatusNoId(
            "Test User",
            testUserEmail,
            StatusEnum.CONFIRMED
        );
        testUser.setPassword(passwordEncoder.encode(Fixtures.RAW_TEST_PASSWORD));
        testUser.setAuthProvider(AuthProvider.LOCAL);
        testUser = usersRepository.save(testUser);

        UserRole.Id userRoleId = new UserRole.Id();
        userRoleId.setUserId(testUser.getId());
        userRoleId.setRoleId(uczenRole.getId());

        UserRole userRole = new UserRole();
        userRole.setId(userRoleId);
        userRole.setUser(testUser);
        userRole.setRole(uczenRole);
        userRoleRepository.save(userRole);
    }

    @Test
    void register_ShouldCreateUser_WhenValidData() throws Exception {
        // Given
        UserRequestDto userRequest = UserRequestDto.builder()
            .fullName("New User")
            .email("newuser@example.com")
            .password(Fixtures.RAW_TEST_PASSWORD)
            .authProvider(AuthProvider.LOCAL)
            .build();

        HttpEntity<UserRequestDto> request = new HttpEntity<>(userRequest);

        // When
        ResponseEntity<UserResponseDto> response = restTemplate.postForEntity(
            "/api/auth/register",
            request,
            UserResponseDto.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New User", response.getBody().getFullName());
        assertEquals("newuser@example.com", response.getBody().getEmail());
        assertEquals(StatusEnum.PENDING, response.getBody().getStatus());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenEmailAlreadyExists() throws Exception {
        // Given
        UserRequestDto userRequest = UserRequestDto.builder()
            .fullName("Duplicate User")
            .email(testUserEmail) // existing email
            .password(Fixtures.RAW_TEST_PASSWORD)
            .authProvider(AuthProvider.LOCAL)
            .build();

        HttpEntity<UserRequestDto> request = new HttpEntity<>(userRequest);

        // When
        ResponseEntity<Void> response = restTemplate.postForEntity(
            "/api/auth/register",
            request,
            Void.class
        );

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void register_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Given
        // validation in requestdto
        UserRequestDto userRequest = UserRequestDto.builder()
            .fullName("")
            .email("invalid-email")
            .password("123")
            .authProvider(AuthProvider.LOCAL)
            .build();

        HttpEntity<UserRequestDto> request = new HttpEntity<>(userRequest);

        // When
        ResponseEntity<Void> response = restTemplate.postForEntity(
            "/api/auth/register",
            request,
            Void.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() throws Exception {
        // Given
        LoginRequestDto loginRequest = LoginRequestDto.builder()
            .email(testUserEmail)
            .password(testUserPassword)
            .build();

        HttpEntity<LoginRequestDto> request = new HttpEntity<>(loginRequest);

        // When
        ResponseEntity<LoginResponseDto> response = restTemplate.postForEntity(
            "/api/auth/login",
            request,
            LoginResponseDto.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getAccessToken());
        assertNull(response.getBody().getRefreshToken());
        assertEquals(testUserEmail, response.getBody().getUser().getEmail());

        assertHasCookie(response, "accessToken");
        assertHasCookie(response, "refreshToken");
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenInvalidCredentials() throws Exception {
        // Given
        LoginRequestDto loginRequest = LoginRequestDto.builder()
            .email(testUserEmail)
            .password("wrongpassword")
            .build();

        HttpEntity<LoginRequestDto> request = new HttpEntity<>(loginRequest);

        // When
        ResponseEntity<Void> response = restTemplate.postForEntity(
            "/api/auth/login",
            request,
            Void.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenUserNotFound() throws Exception {
        // Given
        LoginRequestDto loginRequest = LoginRequestDto.builder()
            .email("nonexistent@example.com")
            .password(Fixtures.RAW_TEST_PASSWORD)
            .build();

        HttpEntity<LoginRequestDto> request = new HttpEntity<>(loginRequest);

        // When
        ResponseEntity<Void> response = restTemplate.postForEntity(
            "/api/auth/login",
            request,
            Void.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void refreshToken_ShouldReturnNewTokens_WhenValidRefreshToken() throws Exception {
        // Given
        LoginRequestDto loginRequest = LoginRequestDto.builder()
            .email(testUserEmail)
            .password(testUserPassword)
            .build();

        HttpEntity<LoginRequestDto> loginRequestEntity = new HttpEntity<>(loginRequest);
        ResponseEntity<LoginResponseDto> loginResponse = restTemplate.postForEntity(
            "/api/auth/login",
            loginRequestEntity,
            LoginResponseDto.class
        );

        String refreshTokenCookie = extractCookieValue(loginResponse, "refreshToken");
        assertNotNull(refreshTokenCookie);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "refreshToken=" + refreshTokenCookie);

        HttpEntity<Void> request = new HttpEntity<>(null, headers);

        // When
        ResponseEntity<LoginResponseDto> response = restTemplate.postForEntity(
            "/api/auth/refresh",
            request,
            LoginResponseDto.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getAccessToken());
        assertNull(response.getBody().getRefreshToken());
        assertEquals(testUserEmail, response.getBody().getUser().getEmail());
        assertHasCookie(response, "accessToken");
        assertHasCookie(response, "refreshToken");
    }

    @Test
    void refreshToken_ShouldReturnUnauthorized_WhenInvalidRefreshToken() throws Exception {
        // Given
        RefreshTokenRequestDto refreshRequest = RefreshTokenRequestDto.builder()
            .refreshToken("invalid-refresh-token")
            .build();

        HttpEntity<RefreshTokenRequestDto> request = new HttpEntity<>(refreshRequest);

        // When
        ResponseEntity<Void> response = restTemplate.postForEntity(
            "/api/auth/refresh",
            request,
            Void.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void logout_ShouldReturnOk_WhenCalledWithoutPrincipal() throws Exception {
        ResponseEntity<Void> response = restTemplate.postForEntity(
            "/api/auth/logout",
            null,
            Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}
