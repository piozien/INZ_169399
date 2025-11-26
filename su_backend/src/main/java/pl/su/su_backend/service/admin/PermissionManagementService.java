package pl.su.su_backend.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.permissions.Permission;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.repositories.permission.PermissionRepository;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.service.log.ActivityLogService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PermissionManagementService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<String> getRolePermissions(RoleCode roleCode) {
        log.info("Fetching permissions for role {}", roleCode);

        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono roli: " + roleCode));

        return role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toList());
    }

    public void assignPermissionToRole(RoleCode roleCode, PermissionCode permissionCode, UUID actingUserId) {
        log.info("Assigning permission {} to role {} by user ID: {}", permissionCode, roleCode, actingUserId);

        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono roli"));

        Permission permission = permissionRepository.findByName(permissionCode.name())
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono uprawnienia: " + permissionCode));

        if (role.getPermissions().contains(permission)) {
            log.info("Permission already assigned");
            return;
        }

        role.getPermissions().add(permission);
        roleRepository.save(role);

        activityLogService.log(actingUserId, ActionType.PERMISSION_UPDATE,
                "Dodano uprawnienie " + permissionCode + " do roli " + roleCode);
    }

    public void revokePermissionFromRole(RoleCode roleCode, PermissionCode permissionCode, UUID actingUserId) {
        log.info("Revoking permission {} from role {} by user ID: {}", permissionCode, roleCode, actingUserId);

        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono roli"));

        Permission permission = permissionRepository.findByName(permissionCode.name())
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono uprawnienia"));

        if (!role.getPermissions().contains(permission)) {
            return;
        }

        role.getPermissions().remove(permission);
        roleRepository.save(role);

        activityLogService.log(actingUserId, ActionType.PERMISSION_UPDATE,
                "Usunięto uprawnienie " + permissionCode + " z roli " + roleCode);
    }

    @Transactional(readOnly = true)
    public Map<RoleCode, List<String>> getPermissionMatrix() {
        Map<RoleCode, List<String>> matrix = new HashMap<>();

        for (RoleCode roleCode : RoleCode.values()) {
            List<String> permissions = getRolePermissions(roleCode);
            matrix.put(roleCode, permissions);
        }

        return matrix;
    }
}