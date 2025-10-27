package pl.su.su_backend.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import pl.su.su_backend.config.JwtConfig;
import pl.su.su_backend.dto.user.*;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;
import pl.su.su_backend.model.enums.*;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.repositories.classRep.ClassesRepository;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.user.UserRoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.auth.TokenService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.testsupport.Fixtures;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private ClassesRepository classesRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtConfig jwtConfig;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokenService;
    @Mock
    private MailService mailService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private UserService userService;

    private Users testUser;
    private Role defaultRole;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "frontendUrl", "http://localhost:3000");

        testUser = Fixtures.user("Test User", "test@test.com");
        testUser.setId(UUID.randomUUID());
        testUser.setStatus(StatusEnum.CONFIRMED);

        defaultRole = Fixtures.role(RoleCode.UCZEN);
        UserRole userRole = Fixtures.userRole(testUser, defaultRole);
        testUser.setUserRoles(Set.of(userRole));
    }

    @Test
    void registerUser_ShouldRegisterSuccessfully_WhenValidData() {
        // Given
        UserRequestDto requestDto = Fixtures.userRequestDto();
        Users savedUser = Fixtures.user();
        savedUser.setId(UUID.randomUUID());

        when(usersRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(usersRepository.save(any(Users.class))).thenReturn(savedUser);
        when(roleRepository.findByRoleCode(RoleCode.UCZEN)).thenReturn(Optional.of(defaultRole));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(new UserRole());
        when(jwtConfig.generateActivationToken(anyString())).thenReturn("activation-token");
        doNothing().when(mailService).sendActivationEmail(anyString(), anyString(), anyString());
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        // When
        UserResponseDto response = userService.registerUser(requestDto);

        // Then
        assertNotNull(response);
        verify(usersRepository).save(any(Users.class));
        verify(userRoleRepository).save(any(UserRole.class));
        verify(mailService).sendActivationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Given
        UserRequestDto requestDto = Fixtures.userRequestDto();
        when(usersRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(testUser));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> userService.registerUser(requestDto));

        assertEquals(ErrorCode.EMAIL_IN_USE, exception.getCode());
    }

    @Test
    void registerUser_ShouldThrowException_WhenPasswordIsEmpty() {
        // Given
        UserRequestDto requestDto = Fixtures.userRequestDto("Test", "test@test.com", "");
        when(usersRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> userService.registerUser(requestDto));

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getCode());
    }


    @Test
    void loginUser_ShouldLoginSuccessfully_WhenValidCredentials() {
        // Given
        LoginRequestDto loginDto = Fixtures.loginRequestDto();
        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(User
                .withUsername(testUser.getEmail())
                .password("password")
                .authorities("ROLE_USER")
                .build());
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtConfig.generateToken(anyString())).thenReturn("access-token");
        when(jwtConfig.generateRefreshToken(anyString())).thenReturn("refresh-token");
        when(jwtConfig.getJwtExpiration()).thenReturn(3600000L);
        doNothing().when(tokenService).saveRefreshToken(any(UUID.class), anyString());
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        // When
        LoginResponseDto response = userService.loginUser(loginDto);

        // Then
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(tokenService).saveRefreshToken(any(UUID.class), anyString());
    }


    @Test
    void refreshToken_ShouldRefreshSuccessfully_WhenValidToken() {
        // Given
        RefreshTokenRequestDto refreshDto = Fixtures.refreshTokenRequestDto("old-refresh-token");

        when(jwtConfig.extractEmail(anyString())).thenReturn(testUser.getEmail());
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenService.isRefreshTokenValid(any(UUID.class), anyString())).thenReturn(true);
        when(jwtConfig.generateToken(anyString())).thenReturn("new-access-token");
        when(jwtConfig.generateRefreshToken(anyString())).thenReturn("new-refresh-token");
        when(jwtConfig.getJwtExpiration()).thenReturn(3600000L);
        doNothing().when(tokenService).saveRefreshToken(any(UUID.class), anyString());
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        // When
        LoginResponseDto response = userService.refreshToken(refreshDto);

        // Then
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenInvalid() {
        // Given
        RefreshTokenRequestDto refreshDto = Fixtures.refreshTokenRequestDto("invalid-token");

        when(jwtConfig.extractEmail(anyString())).thenReturn(testUser.getEmail());
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(tokenService.isRefreshTokenValid(any(UUID.class), anyString())).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> userService.refreshToken(refreshDto));

        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getCode());
    }


    @Test
    void logoutUser_ShouldLogoutSuccessfully() {
        // Given
        UUID userId = UUID.randomUUID();
        doNothing().when(tokenService).revokeRefreshToken(any(UUID.class));
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        // When
        userService.logoutUser(userId);

        // Then
        verify(tokenService).revokeRefreshToken(any(UUID.class));
        verify(activityLogService).log(any(UUID.class), eq(ActionType.LOGOUT), anyString());
    }


    @Test
    void getUserById_ShouldReturnUser_WhenHasPermission() {
        // Given
        UUID userId = UUID.randomUUID();
        Users targetUser = Fixtures.user();
        targetUser.setId(userId);

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_VIEW)).thenReturn(true);
        when(usersRepository.findById(userId)).thenReturn(Optional.of(targetUser));

        // When
        UserResponseDto response = userService.getUserById(userId, testUser.getEmail());

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getId());
    }

    @Test
    void getUserById_ShouldThrowException_WhenNoPermission() {
        // Given
        UUID userId = UUID.randomUUID();

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_VIEW)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> userService.getUserById(userId, testUser.getEmail()));

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenExists() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // When
        UserResponseDto response = userService.getUserByEmail(testUser.getEmail());

        // Then
        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
    }

    @Test
    void getUserByEmail_ShouldThrowException_WhenNotFound() {
        // Given
        when(usersRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> userService.getUserByEmail("nonexistent@test.com"));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getCode());
    }


    @Test
    void updateUser_ShouldUpdateSuccessfully() {
        // Given
        UUID userId = UUID.randomUUID();
        UserRequestDto updateDto = Fixtures.userRequestDto("Updated Name", "updated@test.com", "newpass");
        Users existingUser = Fixtures.user();
        existingUser.setId(userId);

        when(usersRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(usersRepository.save(any(Users.class))).thenReturn(existingUser);
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        // When
        UserResponseDto response = userService.updateUser(userId, updateDto);

        // Then
        assertNotNull(response);
        verify(usersRepository).save(any(Users.class));
    }


    @Test
    void deleteUser_ShouldBlockUser_WhenNotBlocked() {
        // Given
        UUID userId = UUID.randomUUID();
        Users user = Fixtures.user();
        user.setId(userId);
        user.setStatus(StatusEnum.CONFIRMED);

        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        when(usersRepository.save(any(Users.class))).thenReturn(user);
        doNothing().when(tokenService).revokeRefreshToken(any(UUID.class));
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        // When
        userService.deleteUser(userId);

        // Then
        verify(usersRepository).save(any(Users.class));
        verify(tokenService).revokeRefreshToken(any(UUID.class));
    }

    @Test
    void deleteUser_ShouldThrowException_WhenAlreadyBlocked() {
        // Given
        UUID userId = UUID.randomUUID();
        Users user = Fixtures.user();
        user.setId(userId);
        user.setStatus(StatusEnum.BLOCKED);

        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> userService.deleteUser(userId));

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getCode());
    }


    @Test
    void userExists_ShouldReturnTrue_WhenUserExists() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // When
        boolean exists = userService.userExists(testUser.getEmail());

        // Then
        assertTrue(exists);
    }

    @Test
    void userExists_ShouldReturnFalse_WhenUserDoesNotExist() {
        // Given
        when(usersRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // When
        boolean exists = userService.userExists("nonexistent@test.com");

        // Then
        assertFalse(exists);
    }


    @Test
    void loginOAuth2User_ShouldLoginSuccessfully_WhenUserExists() {
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtConfig.generateToken(anyString())).thenReturn("access-token");
        when(jwtConfig.generateRefreshToken(anyString())).thenReturn("refresh-token");
        when(jwtConfig.getJwtExpiration()).thenReturn(3600000L);
        doNothing().when(tokenService).saveRefreshToken(any(UUID.class), anyString());
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        LoginResponseDto response = userService.loginOAuth2User(testUser.getEmail());

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
    }

    @Test
    void loginOAuth2User_ShouldThrowException_WhenUserBlocked() {
        testUser.setStatus(StatusEnum.BLOCKED);
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        ApiException exception = assertThrows(ApiException.class, () -> userService.loginOAuth2User(testUser.getEmail()));

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
    }

    @Test
    void registerOAuth2User_ShouldRegisterSuccessfully_WhenNewUser() {
        String email = "new@test.com";
        String fullName = "New User";
        Users newUser = Fixtures.user(fullName, email);
        newUser.setId(UUID.randomUUID());
        UserRole userRole = Fixtures.userRole(newUser, defaultRole);
        newUser.setUserRoles(Set.of(userRole));

        when(usersRepository.findByEmail(email)).thenReturn(Optional.empty()).thenReturn(Optional.of(newUser));
        when(usersRepository.save(any(Users.class))).thenReturn(newUser);
        when(roleRepository.findByRoleCode(RoleCode.UCZEN)).thenReturn(Optional.of(defaultRole));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(userRole);
        when(jwtConfig.generateToken(anyString())).thenReturn("access-token");
        when(jwtConfig.generateRefreshToken(anyString())).thenReturn("refresh-token");
        when(jwtConfig.getJwtExpiration()).thenReturn(3600000L);
        doNothing().when(mailService).sendWelcomeEmail(anyString(), anyString());
        doNothing().when(tokenService).saveRefreshToken(any(UUID.class), anyString());
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        LoginResponseDto response = userService.registerOAuth2User(email, fullName, "ext-id", AuthProvider.MICROSOFT);

        assertNotNull(response);
        verify(usersRepository).save(any(Users.class));
        verify(mailService).sendWelcomeEmail(anyString(), anyString());
    }

    @Test
    void registerOAuth2User_ShouldThrowException_WhenEmailExists() {
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        ApiException exception = assertThrows(ApiException.class, () -> userService.registerOAuth2User(testUser.getEmail(),
                "Name", "ext-id", AuthProvider.MICROSOFT));

        assertEquals(ErrorCode.EMAIL_IN_USE, exception.getCode());
    }

    @Test
    void loginOrRegisterOAuth2_ShouldLogin_WhenUserExists() {
        testUser.setAuthProvider(AuthProvider.MICROSOFT);
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtConfig.generateToken(anyString())).thenReturn("access-token");
        when(jwtConfig.generateRefreshToken(anyString())).thenReturn("refresh-token");
        when(jwtConfig.getJwtExpiration()).thenReturn(3600000L);
        doNothing().when(tokenService).saveRefreshToken(any(UUID.class), anyString());
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        LoginResponseDto response = userService.loginOrRegisterOAuth2(testUser.getEmail(), "Name",
                "ext-id", AuthProvider.MICROSOFT);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
    }

    @Test
    void loginOrRegisterOAuth2_ShouldThrowException_WhenInvalidProvider() {
        ApiException exception = assertThrows(ApiException.class, () ->
                userService.loginOrRegisterOAuth2("test@test.com", "Name", "ext-id", AuthProvider.LOCAL));

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getCode());
    }

    @Test
    void loginOrRegisterOAuth2_ShouldThrowException_WhenProviderMismatch() {
        testUser.setAuthProvider(AuthProvider.LOCAL);
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        ApiException exception = assertThrows(ApiException.class, () -> userService.loginOrRegisterOAuth2(testUser.getEmail(),
                "Name", "ext-id", AuthProvider.MICROSOFT));

        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getCode());
    }


    @Test
    void getAllUsers_ShouldReturnAllUsers_WhenHasEditPermission() {
        Users adminUser = Fixtures.user("Admin", "admin@test.com");
        adminUser.setId(UUID.randomUUID());
        Users user1 = Fixtures.user("User1", "user1@test.com");
        user1.setId(UUID.randomUUID());
        user1.setStatus(StatusEnum.CONFIRMED);
        Users user2 = Fixtures.user("User2", "user2@test.com");
        user2.setId(UUID.randomUUID());
        user2.setStatus(StatusEnum.BLOCKED);

        when(usersRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(permissionService.hasPermission(adminUser.getId(), PermissionCode.USER_VIEW)).thenReturn(true);
        when(permissionService.hasPermission(adminUser.getId(), PermissionCode.USER_EDIT)).thenReturn(true);
        when(usersRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponseDto> response = userService.getAllUsers(adminUser.getEmail());

        assertEquals(1, response.size());
        assertEquals(user1.getEmail(), response.getFirst().getEmail());
    }

    @Test
    void getAllUsers_ShouldReturnClassUsers_WhenNoEditPermission() {
        Classes testClass = Fixtures.schoolClass("1A", "2025");
        testClass.setId(UUID.randomUUID());
        testUser.setClasses(testClass);
        Users classUser = Fixtures.user("Class User", "class@test.com");
        classUser.setId(UUID.randomUUID());
        classUser.setClasses(testClass);
        classUser.setStatus(StatusEnum.CONFIRMED);

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_VIEW)).thenReturn(true);
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_EDIT)).thenReturn(false);
        when(usersRepository.findByClasses_Id(testClass.getId())).thenReturn(List.of(testUser, classUser));

        List<UserResponseDto> response = userService.getAllUsers(testUser.getEmail());

        assertEquals(2, response.size());
    }

    @Test
    void getAllUsers_ShouldReturnOnlyCurrentUser_WhenNoClass() {
        testUser.setClasses(null);
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_VIEW)).thenReturn(true);
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_EDIT)).thenReturn(false);

        List<UserResponseDto> response = userService.getAllUsers(testUser.getEmail());

        assertEquals(1, response.size());
        assertEquals(testUser.getEmail(), response.getFirst().getEmail());
    }


    @Test
    void blockUser_ShouldBlockUser_WhenHasPermission() {
        UUID targetUserId = UUID.randomUUID();
        Users targetUser = Fixtures.user("Target", "target@test.com");
        targetUser.setId(targetUserId);
        targetUser.setStatus(StatusEnum.CONFIRMED);

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_EDIT)).thenReturn(true);
        when(usersRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(usersRepository.save(any(Users.class))).thenReturn(targetUser);
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        userService.blockUser(targetUserId, testUser.getEmail());

        assertEquals(StatusEnum.BLOCKED, targetUser.getStatus());
        verify(usersRepository).save(targetUser);
    }

    @Test
    void blockUser_ShouldThrowException_WhenNoPermission() {
        UUID targetUserId = UUID.randomUUID();
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_EDIT)).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () -> userService.blockUser(targetUserId, testUser.getEmail()));

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
    }

    @Test
    void blockUser_ShouldThrowException_WhenBlockingSelf() {
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_EDIT)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> userService.blockUser(testUser.getId(), testUser.getEmail()));

        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getCode());
    }

    @Test
    void unblockUser_ShouldUnblockUser_WhenHasPermission() {
        UUID targetUserId = UUID.randomUUID();
        Users targetUser = Fixtures.user("Target", "target@test.com");
        targetUser.setId(targetUserId);
        targetUser.setStatus(StatusEnum.BLOCKED);

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_EDIT)).thenReturn(true);
        when(usersRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(usersRepository.save(any(Users.class))).thenReturn(targetUser);
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        userService.unblockUser(targetUserId, testUser.getEmail());

        assertEquals(StatusEnum.CONFIRMED, targetUser.getStatus());
        verify(usersRepository).save(targetUser);
        verify(activityLogService).log(eq(testUser.getId()), any(), anyString());
    }


    @Test
    void assignUserToClass_ShouldAssignSuccessfully_WhenHasPermission() {
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        Users user = Fixtures.user("User", "user@test.com");
        user.setId(userId);
        Classes classes = Fixtures.schoolClass("1A", "2025");
        classes.setId(classId);

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_EDIT)).thenReturn(true);
        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        when(classesRepository.findById(classId)).thenReturn(Optional.of(classes));
        when(usersRepository.save(any(Users.class))).thenReturn(user);
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        userService.assignUserToClass(userId, classId, testUser.getEmail());

        assertEquals(classes, user.getClasses());
        verify(usersRepository).save(user);
    }

    @Test
    void removeUserFromClass_ShouldRemoveSuccessfully_WhenHasPermission() {
        UUID userId = UUID.randomUUID();
        Users user = Fixtures.user("User", "user@test.com");
        user.setId(userId);
        Classes classes = Fixtures.schoolClass("1A", "2025");
        user.setClasses(classes);

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_EDIT)).thenReturn(true);
        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        when(usersRepository.save(any(Users.class))).thenReturn(user);
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        userService.removeUserFromClass(userId, testUser.getEmail());

        assertNull(user.getClasses());
        verify(usersRepository).save(user);
    }


    @Test
    void getUserRoles_ShouldReturnRoles_WhenUserExists() {
        UUID userId = UUID.randomUUID();
        when(usersRepository.findById(userId)).thenReturn(Optional.of(testUser));

        List<String> roles = userService.getUserRoles(userId);

        assertEquals(1, roles.size());
        assertEquals("UCZEN", roles.getFirst());
    }

    @Test
    void getUserRoles_ShouldThrowException_WhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(usersRepository.findById(userId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                userService.getUserRoles(userId));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getCode());
    }


    @Test
    void getUsersByClass_ShouldReturnActiveUsers_WhenClassExists() {
        UUID classId = UUID.randomUUID();
        Classes classes = Fixtures.schoolClass("1A", "2025");
        classes.setId(classId);
        Users user1 = Fixtures.user("User1", "user1@test.com");
        user1.setId(UUID.randomUUID());
        user1.setClasses(classes);
        user1.setStatus(StatusEnum.CONFIRMED);
        Users user2 = Fixtures.user("User2", "user2@test.com");
        user2.setId(UUID.randomUUID());
        user2.setClasses(classes);
        user2.setStatus(StatusEnum.BLOCKED);

        when(usersRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponseDto> response = userService.getUsersByClass(classId);

        assertEquals(1, response.size());
        assertEquals(user1.getEmail(), response.getFirst().getEmail());
    }

    @Test
    void updateUserWithPermissions_ShouldUpdate_WhenHasEditPermission() {
        UUID userId = UUID.randomUUID();
        UserRequestDto updateDto = Fixtures.userRequestDto("Updated", "updated@test.com", "newpass");
        Users existingUser = Fixtures.user();
        existingUser.setId(userId);

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_VIEW)).thenReturn(true);
        when(usersRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(usersRepository.save(any(Users.class))).thenReturn(existingUser);
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        UserResponseDto response = userService.updateUser(userId, updateDto, testUser.getEmail());

        assertNotNull(response);
        verify(usersRepository).save(any(Users.class));
    }

    @Test
    void updateUserWithPermissions_ShouldUpdate_WhenUpdatingSelf() {
        UserRequestDto updateDto = Fixtures.userRequestDto("Updated", "updated@test.com", "newpass");

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_VIEW)).thenReturn(false);
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(usersRepository.save(any(Users.class))).thenReturn(testUser);
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        UserResponseDto response = userService.updateUser(testUser.getId(), updateDto, testUser.getEmail());

        assertNotNull(response);
        verify(usersRepository).save(any(Users.class));
    }

    @Test
    void updateUserWithPermissions_ShouldThrowException_WhenNoPermissionAndNotSelf() {
        UUID otherUserId = UUID.randomUUID();
        UserRequestDto updateDto = Fixtures.userRequestDto();

        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.USER_VIEW)).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () -> userService.updateUser(otherUserId, updateDto, testUser.getEmail()));

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
    }
}