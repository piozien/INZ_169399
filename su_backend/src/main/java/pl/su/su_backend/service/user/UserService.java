package pl.su.su_backend.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.config.JwtConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;
import pl.su.su_backend.dto.user.*;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.user.UserRoleRepository;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.repositories.classRep.ClassesRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.service.auth.TokenService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UsersRepository usersRepository;
    private final ClassesRepository classesRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final MailService mailService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final ActivityLogService activityLogService;
    private final PermissionService permissionService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public UserResponseDto registerUser(UserRequestDto userRequestDto) {
        log.info("Registering new user with email: {}", userRequestDto.getEmail());

        if (usersRepository.findByEmail(userRequestDto.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + userRequestDto.getEmail() + " already exists");
        }

        Users user = Users.builder()
                .fullName(userRequestDto.getFullName())
                .email(userRequestDto.getEmail())
                .password(userRequestDto.getPassword() != null && !userRequestDto.getPassword().isEmpty()
                        ? passwordEncoder.encode(userRequestDto.getPassword())
                        : "") // empty password OAUTH2
                .status(userRequestDto.getStatus() != null ? userRequestDto.getStatus() : StatusEnum.PENDING)
                .classes(null)
                .authProvider(userRequestDto.getAuthProvider() != null ? userRequestDto.getAuthProvider() : AuthProvider.LOCAL)
                .externalId(userRequestDto.getExternalId())
                .createdAt(LocalDateTime.now())
                .build();

        Users savedUser = usersRepository.save(user);
        assignDefaultRole(savedUser);
        sendActivationEmail(savedUser);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        activityLogService.log(savedUser.getId(), ActionType.REGISTER, "User registered");

        return UserMapper.toResponseDto(savedUser);
    }


    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto) {
        log.info("Attempting login for user: {}", loginRequestDto.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Users user = usersRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("User logged in successfully: {}", user.getEmail());
        activityLogService.log(user.getId(), ActionType.LOGIN, "User logged in");

        return buildLoginResponse(user);
    }


    public LoginResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        log.info("Refreshing token");

        String email = jwtConfig.extractEmail(refreshTokenRequestDto.getRefreshToken());
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!tokenService.isRefreshTokenValid(user.getId(), refreshTokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("Invalid refresh token");
        }


        String newAccessToken = jwtConfig.generateToken(user.getEmail());
        String newRefreshToken = jwtConfig.generateRefreshToken(user.getEmail());

        tokenService.saveRefreshToken(user.getId(), newRefreshToken);

        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleCode().name())
                .collect(Collectors.toList());

        log.info("Token refreshed successfully for user: {}", user.getEmail());

        activityLogService.log(user.getId(), ActionType.LOGIN, "User refreshed token");

        return LoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getJwtExpiration() / 1000)
                .user(UserMapper.toResponseDto(user))
                .roles(roles)
                .build();
    }

    public void logoutUser(UUID userId) {
        log.info("Logging out user with ID: {}", userId);
        tokenService.revokeRefreshToken(userId);
        activityLogService.log(userId, ActionType.LOGOUT, "User logged out");
        log.info("User logged out successfully");
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID userId, String currentUserEmail) {
        log.info("Fetching user with ID: {} by user: {}", userId, currentUserEmail);

        Users currentUser = getCurrentUser(currentUserEmail);

        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.USER_VIEW)) {
            throw new RuntimeException("Access denied: User must have user viewing permission");
        }

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return UserMapper.toResponseDto(user);
    }


    @Transactional(readOnly = true)
    public List<String> getUserRoles(UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return user.getUserRoles().stream()
                .map(ur -> ur.getRole().getRoleCode().name())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return UserMapper.toResponseDto(user);
    }

    public UUID getCurrentUserId(String email) {
        return getUserByEmail(email).getId();
    }

    public List<UserResponseDto> getAllUsers(String currentUserEmail) {
        log.info("Fetching all users for: {}", currentUserEmail);

        Users currentUser = getCurrentUser(currentUserEmail);

        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.USER_VIEW)) {
            throw new RuntimeException("Access denied: User must have user viewing permission");
        }

        if (permissionService.hasPermission(currentUser.getId(), PermissionCode.USER_EDIT)) {
            return usersRepository.findAll().stream()
                    .filter(user -> !user.isBlocked())
                    .map(UserMapper::toResponseDto)
                    .collect(Collectors.toList());
        } else {
            if (currentUser.getClasses() == null) {
                return List.of(UserMapper.toResponseDto(currentUser));
            }

            return usersRepository.findByClasses_Id(currentUser.getClasses().getId()).stream()
                    .filter(user -> user.getStatus() != StatusEnum.BLOCKED)
                    .map(UserMapper::toResponseDto)
                    .collect(Collectors.toList());
        }
    }

    public UserResponseDto updateUser(UUID userId, UserRequestDto userRequestDto) {
        log.info("Updating user with ID: {}", userId);

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setFullName(userRequestDto.getFullName());
        user.setEmail(userRequestDto.getEmail());
        if (userRequestDto.getPassword() != null && !userRequestDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        }
        if (userRequestDto.getStatus() != null) {
            user.setStatus(userRequestDto.getStatus());
        }

        Users updatedUser = usersRepository.save(user);
        activityLogService.log(updatedUser.getId(), ActionType.UPDATE_PROFILE, "User profile updated");
        log.info("User updated successfully with ID: {}", updatedUser.getId());

        return UserMapper.toResponseDto(updatedUser);
    }

    public void deleteUser(UUID userId) {
        log.info("Soft deleting (blocking) user with ID: {}", userId);

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.isBlocked()) {
            throw new RuntimeException("User is already deleted/blocked");
        }

        user.setStatus(StatusEnum.BLOCKED);
        usersRepository.save(user);

        tokenService.revokeRefreshToken(userId);
        activityLogService.log(userId, ActionType.SOFT_DELETE, "User soft deleted (blocked)");

        log.info("User soft deleted (blocked) successfully with ID: {}", userId);
    }

    @Transactional(readOnly = true)
    public boolean userExists(String email) {
        return usersRepository.findByEmail(email).isPresent();
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsersByClass(UUID classId) {
        log.info("Fetching active users for class ID: {}", classId);
        return usersRepository.findAll().stream()
                .filter(user -> !user.isBlocked())
                .filter(user -> user.getClasses() != null && classId.equals(user.getClasses().getId()))
                .map(UserMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public LoginResponseDto loginOAuth2User(String email) {
        log.info("OAuth2 login for existing user: {}", email);

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.isBlocked()) {
            throw new RuntimeException("User account is blocked");
        }


        String accessToken = jwtConfig.generateToken(user.getEmail());
        String refreshToken = jwtConfig.generateRefreshToken(user.getEmail());

        tokenService.saveRefreshToken(user.getId(), refreshToken);

        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleCode().name())
                .collect(Collectors.toList());

        log.info("OAuth2 user logged in successfully: {}", user.getEmail());
        activityLogService.log(user.getId(), ActionType.LOGIN, "OAuth2 user logged in");

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getJwtExpiration() / 1000)
                .user(UserMapper.toResponseDto(user))
                .roles(roles)
                .build();
    }

    public LoginResponseDto registerOAuth2User(String email, String fullName, String externalId, AuthProvider authProvider) {
        log.info("OAuth2 registration for new user: {}", email);

        if (usersRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User with email " + email + " already exists");
        }

        Users user = Users.builder()
                .fullName(fullName != null ? fullName : email)
                .email(email)
                .password("")
                .status(StatusEnum.CONFIRMED)
                .authProvider(authProvider)
                .externalId(externalId)
                .createdAt(LocalDateTime.now())
                .build();

        Users savedUser = usersRepository.save(user);
        Role defaultRole = roleRepository.findByRoleCode(RoleCode.UCZEN)
                .orElseThrow(() -> new RuntimeException("Default role UCZEN not found"));
        UserRole userRole = UserRole.builder()
                .id(new UserRole.Id(savedUser.getId(), defaultRole.getId()))
                .user(savedUser)
                .role(defaultRole)
                .build();
        userRoleRepository.save(userRole);
        mailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());
        log.info("OAuth2 user registered successfully with ID: {}", savedUser.getId());

        return loginOAuth2User(email);
    }

    public UserResponseDto updateUser(UUID userId, UserRequestDto userRequestDto, String currentUserEmail) {
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (permissionService.hasPermission(currentUser.getId(), PermissionCode.USER_VIEW)) {

            return updateUser(userId, userRequestDto);
        }

        if (!currentUser.getId().equals(userId)) {
            throw new RuntimeException("You can only edit your own profile");
        }

        return updateUser(userId, userRequestDto);
    }


    public UserResponseDto blockUser(UUID userId, String currentUserEmail) {
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.USER_EDIT)) {
            throw new RuntimeException("You don't have permission to block users");
        }

        if (currentUser.getId().equals(userId)) {
            throw new RuntimeException("You cannot block yourself");
        }

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setStatus(StatusEnum.BLOCKED);
        usersRepository.save(user);
        activityLogService.log(currentUser.getId(), ActionType.USER_BLOCKED, "Blocked user: " + user.getEmail());

        return UserMapper.toResponseDto(user);
    }

    public UserResponseDto unblockUser(UUID userId, String currentUserEmail) {
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.USER_EDIT)) {
            throw new RuntimeException("You don't have permission to unblock users");
        }

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setStatus(StatusEnum.CONFIRMED);
        usersRepository.save(user);
        activityLogService.log(currentUser.getId(), ActionType.USER_UNBLOCKED, "Unblocked user: " + user.getEmail());

        return UserMapper.toResponseDto(user);
    }

    public UserResponseDto assignUserToClass(UUID userId, UUID classId, String currentUserEmail) {
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.USER_EDIT)) {
            throw new RuntimeException("You don't have permission to assign users to classes");
        }

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Classes classes = classesRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found with ID: " + classId));

        user.setClasses(classes);
        usersRepository.save(user);
        activityLogService.log(currentUser.getId(), ActionType.USER_UPDATED, "Assigned user " + user.getEmail() +
                " to class " + classes.getName());

        return UserMapper.toResponseDto(user);
    }

    public UserResponseDto removeUserFromClass(UUID userId, String currentUserEmail) {
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.USER_EDIT)) {
            throw new RuntimeException("You don't have permission to remove users from classes");
        }

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        String className = user.getClasses() != null ? user.getClasses().getName() : "unknown";
        user.setClasses(null);
        usersRepository.save(user);
        activityLogService.log(currentUser.getId(), ActionType.USER_UPDATED, "Removed user " + user.getEmail() +
                " from class " + className);

        return UserMapper.toResponseDto(user);
    }

    private Users getCurrentUser(String currentUserEmail) {
        return usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
    }

    private void assignDefaultRole(Users user) {
        Role defaultRole = roleRepository.findByRoleCode(RoleCode.UCZEN)
                .orElseThrow(() -> new RuntimeException("Default role UCZEN not found"));
        UserRole userRole = UserRole.builder()
                .id(new UserRole.Id(user.getId(), defaultRole.getId()))
                .user(user)
                .role(defaultRole)
                .build();
        userRoleRepository.save(userRole);
    }

    private void sendActivationEmail(Users user) {
        String activationToken = jwtConfig.generateActivationToken(user.getEmail());
        String activationUrl = UriComponentsBuilder
                .fromHttpUrl(frontendUrl)
                .path("/activate")
                .queryParam("token", activationToken)
                .build()
                .toUriString();
        mailService.sendActivationEmail(user.getEmail(), user.getFullName(), activationUrl);
    }

    private LoginResponseDto buildLoginResponse(Users user) {
        String accessToken = jwtConfig.generateToken(user.getEmail());
        String refreshToken = jwtConfig.generateRefreshToken(user.getEmail());
        tokenService.saveRefreshToken(user.getId(), refreshToken);

        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleCode().name())
                .collect(Collectors.toList());

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getJwtExpiration() / 1000)
                .user(UserMapper.toResponseDto(user))
                .roles(roles)
                .build();
    }

}
