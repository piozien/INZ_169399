package pl.su.su_backend.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import pl.su.su_backend.dto.user.UserMapper;
import pl.su.su_backend.dto.user.UserRequestDto;
import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.JwtService;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final ActivityLogService activityLogService;
    private final PermissionService permissionService;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Transactional
    public Users getOrCreateMicrosoftUser(String email, String fullName, String externalId) {
        Optional<Users> existingUser = usersRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            Users user = existingUser.get();

            if (user.isBlocked()) {
                log.warn("Login attempt failed: User {} is currently blocked.", email);
                throw ApiException.forbidden("Konto zablokowane. Skontaktuj się z administratorem.");
            }

            if (user.getAuthProvider() == AuthProvider.LOCAL) {
                log.warn("Account UPGRADE: Converting local account {} to Microsoft account.", email);

                user.setExternalId(externalId);
                user.setAuthProvider(AuthProvider.MICROSOFT);
                user.setPassword(null);
                user.setStatus(StatusEnum.CONFIRMED);

                if (user.getFullName() == null || user.getFullName().isEmpty()) {
                    user.setFullName(fullName);
                }

                Users updatedUser = usersRepository.save(user);
                activityLogService.log(updatedUser.getId(), ActionType.UPDATE_PROFILE, "Konto lokalne połączone z Microsoft");

                return updatedUser;
            }
            return user;
        }

        return createMicrosoftUser(email, fullName, externalId);
    }

    @Transactional
    public UserResponseDto registerLocalUser(UserRequestDto request) {
        log.info("Registering local user: {}", request.getEmail());

        if (usersRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("Adres e-mail jest już używany.");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw ApiException.badRequest("Hasło jest wymagane do rejestracji.");
        }

        Users user = Users.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword().trim()))
                .authProvider(AuthProvider.LOCAL)
                .status(StatusEnum.PENDING)
                .userRoles(new HashSet<>())
                .build();

        Users savedUser = usersRepository.save(user);

        assignDefaultRole(savedUser);
        sendActivationEmail(savedUser);

        activityLogService.log(savedUser.getId(), ActionType.REGISTER, "Użytkownik zarejestrowany lokalnei.");

        return userMapper.toResponseDto(savedUser);
    }

    private Users createMicrosoftUser(String email, String fullName, String externalId) {
        log.info("Creating new Microsoft user: {}", email);

        Users newUser = Users.builder()
                .email(email)
                .fullName(fullName)
                .externalId(externalId)
                .authProvider(AuthProvider.MICROSOFT)
                .status(StatusEnum.CONFIRMED) // MS trusted
                .password(null)
                .userRoles(new HashSet<>())
                .build();

        Users savedUser = usersRepository.save(newUser);
        assignDefaultRole(savedUser);

        mailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());
        activityLogService.log(savedUser.getId(), ActionType.REGISTER, "Użytkownik zarejestrowany przez Microsoft.");

        return savedUser;
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono użytkownika"));

        return userMapper.toResponseDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers(String currentUserEmail) {
        Users currentUser = getUserByEmailEntity(currentUserEmail);

        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.USER_VIEW)) {
            throw ApiException.forbidden("Odmowa dostępu");
        }

        return usersRepository.findAll().stream()
                .filter(user -> !user.isBlocked())
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getUserRoles(UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono użytkownika"));

        return user.getUserRoles().stream()
                .map(ur -> ur.getRole().getRoleCode().name())
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDto updateUser(UUID userId, UserRequestDto request, String currentUserEmail) {
        Users currentUser = getUserByEmailEntity(currentUserEmail);

        boolean canEdit = currentUser.getId().equals(userId) ||
                permissionService.hasPermission(currentUser.getId(), PermissionCode.USER_EDIT);

        if (!canEdit) {
            throw ApiException.forbidden("Odmowa dostępu");
        }

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono użytkownika"));

        user.setFullName(request.getFullName());
        if (request.getStatus() != null && permissionService.hasPermission(currentUser.getId(), PermissionCode.USER_EDIT)) {
            user.setStatus(request.getStatus());
        }

        Users updatedUser = usersRepository.save(user);
        activityLogService.log(updatedUser.getId(), ActionType.UPDATE_PROFILE, "Profil użytkownika zaktualizowany");

        return userMapper.toResponseDto(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID userId, String currentUserEmail) {
        Users currentUser = getUserByEmailEntity(currentUserEmail);

        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.USER_DELETE)) {
            throw ApiException.forbidden("Odmowa dostępu");
        }

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono użytkownika"));

        user.setStatus(StatusEnum.BLOCKED); // Soft delete
        usersRepository.save(user);

        activityLogService.log(userId, ActionType.SOFT_DELETE, "User blocked");
    }

    @Transactional
    public UserResponseDto unblockUser(UUID userId, String currentUserEmail) {
        Users currentUser = getUserByEmailEntity(currentUserEmail);

        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.USER_EDIT)) {
            throw ApiException.forbidden("Brak uprawnień");
        }

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Użytkownik nie istnieje"));

        user.setStatus(StatusEnum.CONFIRMED);
        usersRepository.save(user);

        activityLogService.log(userId, ActionType.USER_UNBLOCKED, "Użytkownik odblokowany");

        return userMapper.toResponseDto(user);
    }


    public Users getUserByEmailEntity(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("Użytkownik nieznaleziony"));
    }

    private void assignDefaultRole(Users user) {
        Role defaultRole = roleRepository.findByRoleCode(RoleCode.UCZEN)
                .orElseThrow(() -> {
                    log.error("CRITICAL: Default role UCZEN missing in DB");
                    return ApiException.notFound(ErrorCode.DEFAULT_ROLE_MISSING, "Błąd systemu: Brakuje domyślnej roli");
                });

        UserRole assignment = new UserRole();
        assignment.setId(new UserRole.Id(user.getId(), defaultRole.getId()));
        assignment.setUser(user);
        assignment.setRole(defaultRole);
        assignment.setAssignedAt(LocalDateTime.now());

        user.getUserRoles().add(assignment);
    }

    private void sendActivationEmail(Users user) {
        try {
            String activationToken = jwtService.generateActivationToken(user.getEmail());
            String activationUrl = UriComponentsBuilder
                    .fromUriString(frontendUrl)
                    .path("/activate")
                    .queryParam("token", activationToken)
                    .build()
                    .toUriString();

            mailService.sendActivationEmail(user.getEmail(), user.getFullName(), activationUrl);
        } catch (Exception e) {
            log.error("Failed to send activation email: {}", e.getMessage());
        }
    }
    public UUID getCurrentUserId(String email) {
        return getUserByEmailEntity(email).getId();
    }

}