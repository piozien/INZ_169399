package pl.su.su_backend.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.user.UserRoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;

import java.util.Comparator;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleAssignmentService {

    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final ActivityLogService activityLogService;
    private final PermissionService permissionService;

    public void assignRoleByEmail(String actingEmail, UUID targetUserId, RoleCode roleCode, String reason) {
        Users acting = usersRepository.findByEmail(actingEmail)
                .orElseThrow(() -> new RuntimeException("Acting user not found: " + actingEmail));
        assignRole(acting.getId(), targetUserId, roleCode, reason);
    }

    public void revokeRoleByEmail(String actingEmail, UUID targetUserId, RoleCode roleCode, String reason) {
        Users acting = usersRepository.findByEmail(actingEmail)
                .orElseThrow(() -> new RuntimeException("Acting user not found: " + actingEmail));
        revokeRole(acting.getId(), targetUserId, roleCode, reason);
    }

    public void assignRole(UUID actingUserId, UUID targetUserId, RoleCode roleCode, String reason) {
        Users acting = getUserOrThrow(actingUserId);
        Users target = getUserOrThrow(targetUserId);
        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleCode));

        if (target.isBlocked()) {
            throw new RuntimeException("Cannot modify roles of blocked user");
        }

        if (!permissionService.hasPermission(acting.getId(), PermissionCode.USER_ASSIGN_ROLE)) {
            throw new RuntimeException("You are not allowed to assign roles");
        }

        if (userRoleRepository.existsByUser_IdAndRole_Id(target.getId(), role.getId())) {
            log.info("Role {} already assigned to user {}", roleCode, target.getEmail());
            return;
        }

        UserRole userRole = UserRole.builder()
                .id(new UserRole.Id(target.getId(), role.getId()))
                .user(target)
                .role(role)
                .build();
        userRoleRepository.save(userRole);
        activityLogService.log(actingUserId, ActionType.ASSIGN_ROLE, "Assigned role " +
                roleCode + " to user " + targetUserId);

        log.info("Role {} assigned to user {} by {}. Reason: {}", roleCode, target.getEmail(), acting.getEmail(), reason);
    }

    public void revokeRole(UUID actingUserId, UUID targetUserId, RoleCode roleCode, String reason) {
        Users acting = getUserOrThrow(actingUserId);
        Users target = getUserOrThrow(targetUserId);
        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleCode));

        if (!permissionService.hasPermission(acting.getId(), PermissionCode.USER_ASSIGN_ROLE)) {
            throw new RuntimeException("You are not allowed to revoke roles");
        }

        RoleCode actingHighestRole = getHighestRole(acting);
        RoleCode targetHighestRole = getHighestRole(target);
        
        if (!actingHighestRole.hasHigherOrEqualRankThan(targetHighestRole)) {
            throw new RuntimeException("You cannot revoke roles from users with higher or equal rank than yours");
        }

        if (roleCode == RoleCode.ADMINISTRATOR) {
            long admins = userRoleRepository.findByRole_Id(role.getId()).size();
            if (admins <= 1) {
                throw new RuntimeException("Cannot revoke the last ADMINISTRATOR role");
            }
        }

        if (!userRoleRepository.existsByUser_IdAndRole_Id(target.getId(), role.getId())) {
            log.info("User {} does not have role {}", target.getEmail(), roleCode);
            return;
        }

        userRoleRepository.deleteByUser_IdAndRole_Id(target.getId(), role.getId());
        activityLogService.log(actingUserId, ActionType.REMOVE_ROLE, "Revoked role " + roleCode + " from user " + targetUserId);
        log.info("Role {} revoked from user {} by {}. Reason: {}", roleCode, target.getEmail(), acting.getEmail(), reason);
    }

    private Users getUserOrThrow(UUID userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }


    private RoleCode getHighestRole(Users user) {
        return user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleCode())
                .max(Comparator.comparingInt(RoleCode::getRank))
                .orElse(RoleCode.UCZEN);
    }

}


