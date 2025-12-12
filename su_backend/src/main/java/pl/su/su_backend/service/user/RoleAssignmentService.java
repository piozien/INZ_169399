package pl.su.su_backend.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.RoleCategory;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.user.UserRoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.log.ActivityLogService;

import java.util.Comparator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleAssignmentService {

    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final ActivityLogService activityLogService;


    @Transactional
    public void assignRoleByEmail(String actingEmail, UUID targetUserId, RoleCode roleCode, String reason) {
        Users acting = getUserEntity(actingEmail);
        Users target = getUserEntity(targetUserId);
        assignRole(acting, target, roleCode, reason);
    }

    @Transactional
    public void revokeRoleByEmail(String actingEmail, UUID targetUserId, RoleCode roleCode, String reason) {
        Users acting = getUserEntity(actingEmail);
        Users target = getUserEntity(targetUserId);
        revokeRole(acting, target, roleCode, reason);
    }


    private void assignRole(Users acting, Users target, RoleCode roleCode, String reason) {
        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono roli: " + roleCode));

        if (role.getCategory() == RoleCategory.SU) {
            throw ApiException.badRequest("Role samorządowe (SU) muszą być nadawane przez moduł Samorządu.");
        }

        if (target.isBlocked()) {
            throw ApiException.badRequest("Nie można edytować ról zablokowanego użytkownika.");
        }

        if (userRoleRepository.existsByUser_IdAndRole_Id(target.getId(), role.getId())) {
            log.info("User {} already has the role {}", target.getEmail(), roleCode);
            return;
        }

        UserRole userRole = UserRole.builder()
                .id(new UserRole.Id(target.getId(), role.getId()))
                .user(target)
                .role(role)
                .build();

        userRoleRepository.save(userRole);

        activityLogService.log(acting.getId(), ActionType.ASSIGN_ROLE,
                "Nadano rolę " + roleCode + " przez:  " + acting.getEmail());

        log.info("Role {} assigned to user {} by {}. Reason: {}", roleCode, target.getEmail(), acting.getEmail(), reason);
    }

    private void revokeRole(Users acting, Users target, RoleCode roleCode, String reason) {
        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono roli: " + roleCode));

        RoleCode actingHighestRole = getHighestRole(acting);
        RoleCode targetHighestRole = getHighestRole(target);

        if (!actingHighestRole.hasHigherOrEqualRankThan(targetHighestRole) && actingHighestRole != RoleCode.ADMINISTRATOR) {
            throw ApiException.forbidden("Nie możesz modyfikować uprawnień użytkownika o wyższej lub równej randze.");
        }

        if (roleCode == RoleCode.ADMINISTRATOR) {
            long adminCount = userRoleRepository.findByRole_Id(role.getId()).size();
            if (adminCount <= 1) {
                throw ApiException.badRequest("Nie można usunąć ostatniego administratora w systemie.");
            }
        }

        UserRole.Id id = new UserRole.Id(target.getId(), role.getId());

        if (!userRoleRepository.existsById(id)) {
            log.info("User {} does not have a role {}", target.getEmail(), roleCode);
            return;
        }

        userRoleRepository.deleteById(id);

        target.getUserRoles().removeIf(ur -> ur.getRole().getRoleCode() == roleCode);

        activityLogService.log(acting.getId(), ActionType.REMOVE_ROLE,
                "Odebrano rolę " + roleCode + " przez " + acting.getEmail());

        log.info("Role {} revoked from user {} by {}. Reason: {}", roleCode, target.getEmail(), acting.getEmail(), reason);
    }


    private Users getUserEntity(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("Użytkownik nie istnieje: " + email));
    }

    private Users getUserEntity(UUID id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Użytkownik nie istnieje: " + id));
    }

    private RoleCode getHighestRole(Users user) {
        return user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleCode())
                .max(Comparator.comparingInt(RoleCode::getRank))
                .orElse(RoleCode.UCZEN);
    }
}