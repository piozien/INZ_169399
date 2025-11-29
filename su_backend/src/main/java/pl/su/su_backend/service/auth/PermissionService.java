package pl.su.su_backend.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.auth.UserPermissionsResponse;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.council.CouncilMemberRepository;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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

    public boolean hasPermission(UUID userId, PermissionCode permission) {
        return hasPermission(userId, permission, null);
    }

    public boolean hasPermission(String userEmail, PermissionCode permission) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> ApiException.badRequest("Nie znaleziono użytkownika"));
        return hasPermission(user.getId(), permission, null);
    }

    public boolean hasPermission(UUID userId, PermissionCode permission, UUID targetCouncilId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.badRequest("Nie znaleziono użytkownika"));

        if (user.getStatus() != StatusEnum.CONFIRMED) {
            log.warn("User {} is not confirmed.", user.getEmail());
            return false;
        }

        Set<Role> activeRoles = new HashSet<>();
        if (user.getUserRoles() != null) {
            user.getUserRoles().forEach(ur -> activeRoles.add(ur.getRole()));
        }

        if (activeRoles.stream().anyMatch(r -> RoleCode.ADMINISTRATOR.equals(r.getRoleCode()))) {
            log.trace("ACCESS GRANTED (GOD MODE): User {} is ADMINISTRATOR.", user.getEmail());
            return true;
        }

        if (targetCouncilId != null) {
            Optional<CouncilMember> membership = councilMemberRepository.findByCouncilIdAndUserId(targetCouncilId, userId);

            if (membership.isPresent()) {
                RoleCode localRoleCode = membership.get().getRole();
                roleRepository.findByRoleCode(localRoleCode).ifPresent(activeRoles::add);
            }
        }

        return checkRolesForPermission(activeRoles, permission);
    }


    public UserPermissionsResponse getUserPermissions(String userEmail) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> ApiException.badRequest("Nie znaleziono użytkownika"));

        Set<Role> allRoles = collectAllUserRolesForInfoOnly(user);

        Set<String> roleNames = allRoles.stream()
                .map(role -> role.getRoleCode().name())
                .collect(Collectors.toSet());

        Set<String> permissions = allRoles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());

        if (isSystemAdmin(user)) {
            permissions.add("ALL_ACCESS");
            roleNames.add("ADMINISTRATOR");
        }

        return new UserPermissionsResponse(roleNames, permissions);
    }


    private boolean isSystemAdmin(Users user) {
        return user.getUserRoles().stream()
                .anyMatch(ur -> RoleCode.ADMINISTRATOR.equals(ur.getRole().getRoleCode()));
    }

    private boolean checkRolesForPermission(Set<Role> roles, PermissionCode permission) {
        return roles.stream()
                .flatMap(r -> r.getPermissions().stream())
                .anyMatch(p -> p.getName().equals(permission.name()));
    }

    private Set<Role> collectAllUserRolesForInfoOnly(Users user) {
        Set<Role> allRoles = new HashSet<>();

        if (user.getUserRoles() != null) {
            user.getUserRoles().forEach(ur -> allRoles.add(ur.getRole()));
        }

        List<CouncilMember> councilMemberships = councilMemberRepository.findByIdUserId(user.getId());
        for (CouncilMember membership : councilMemberships) {
            roleRepository.findByRoleCode(membership.getRole())
                    .ifPresent(allRoles::add);
        }

        return allRoles;
    }
}