package pl.su.su_backend.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.user.UserPermissionsResponse;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCategory;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.council.CouncilMemberRepository;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionService {

    private final UsersRepository usersRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final RoleRepository roleRepository;


    public UserPermissionsResponse getUserPermissions(String userEmail) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));

        Set<Role> allRoles = collectAllUserRoles(user);

        Set<String> roleNames = allRoles.stream()
                .map(role -> role.getRoleCode().name())
                .collect(Collectors.toSet());

        Set<String> permissions = allRoles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());

        return new UserPermissionsResponse(roleNames, permissions);
    }

    public boolean hasPermission(String userEmail, PermissionCode permission) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found: " + userEmail));

        log.info("Checking permission for user: {} permission: {}", userEmail, permission);
        boolean result = hasPermission(user.getId(), permission);
        log.info("Permission result: {}", result);
        return result;
    }

    public boolean hasPermission(UUID userId, PermissionCode permission) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found: " + userId));

        if (user.getStatus() != StatusEnum.CONFIRMED) {
            log.warn("User {} attempted to access resource with status: {}. Account must be activated.",
                    user.getEmail(), user.getStatus());
            return false;
        }

        Set<Role> allRoles = collectAllUserRoles(user);

        log.info("User {} has {} total roles (global + council)", user.getEmail(), allRoles.size());

        boolean result = allRoles.stream()
                .anyMatch(role -> {
                    return role.getPermissions().stream()
                            .anyMatch(permissionEntity -> permissionEntity.getName().equals(permission.getCode()));
                });

        log.info("Final permission result: {}", result);
        return result;
    }

    public boolean canAccessClassBudget(UUID userId, UUID classId, PermissionCode permission) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found: " + userId));

        if (!hasPermission(userId, permission)) {
            return false;
        }

        Set<Role> allRoles = collectAllUserRoles(user);

        return allRoles.stream()
                .anyMatch(role -> {
                    RoleCode roleCode = role.getRoleCode();
                    if (roleCode.getCategory() == RoleCategory.CLASS) {
                        return user.getClasses() != null &&
                                user.getClasses().getId().equals(classId);
                    }
                    return true;
                });
    }

    private Set<Role> collectAllUserRoles(Users user) {
        Set<Role> allRoles = new HashSet<>();

        if (user.getUserRoles() != null) {
            user.getUserRoles().stream()
                    .map(userRole -> userRole.getRole())
                    .forEach(allRoles::add);
        }
        log.info("User {} has {} global roles", user.getEmail(), allRoles.size());

        List<CouncilMember> councilMemberships = councilMemberRepository.findByIdUserId(user.getId());
        log.info("User {} has {} council memberships", user.getEmail(), councilMemberships.size());

        for (CouncilMember membership : councilMemberships) {
            RoleCode councilRoleCode = membership.getRole();
            roleRepository.findByRoleCode(councilRoleCode)
                    .ifPresent(role -> {
                        allRoles.add(role);
                        log.info("Added council role: {} for user {}", councilRoleCode, user.getEmail());
                    });
        }

        return allRoles;
    }
}