package pl.su.su_backend.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.permissions.Permission;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.repositories.permission.PermissionRepository;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PermissionManagementService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionService permissionService;

    @Transactional(readOnly = true)
    public List<String> getRolePermissions(RoleCode roleCode, String currentUserEmail) {
        log.info("Fetching permissions for role {} by user: {}", roleCode, currentUserEmail);

        if (!permissionService.hasPermission(currentUserEmail, PermissionCode.USER_ASSIGN_ROLE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Role not found"));

        return role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PermissionCode> getAllPermissions(String currentUserEmail) {
        log.info("Fetching all permissions by user: {}", currentUserEmail);
        
        if (!permissionService.hasPermission(currentUserEmail, PermissionCode.USER_ASSIGN_ROLE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        return Arrays.asList(PermissionCode.values());
    }

    @Transactional(readOnly = true)
    public List<RoleCode> getAllRoles(String currentUserEmail) {
        log.info("Fetching all roles by user: {}", currentUserEmail);
        
        if (!permissionService.hasPermission(currentUserEmail, PermissionCode.USER_ASSIGN_ROLE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        return Arrays.asList(RoleCode.values());
    }

    public void assignPermissionToRole(RoleCode roleCode, PermissionCode permissionCode, String currentUserEmail) {
        log.info("Assigning permission {} to role {} by user: {}", permissionCode, roleCode, currentUserEmail);
        
        if (!permissionService.hasPermission(currentUserEmail, PermissionCode.USER_ASSIGN_ROLE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied: User must have permission:" +
                    " user assign");
        }

        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Role not found"));

        Permission permission = permissionRepository.findByName(permissionCode.getCode())
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Permission not found"));

        if (role.getPermissions().contains(permission)) {
            log.info("Permission {} already assigned to role {}", permissionCode, roleCode);
            return;
        }

        role.getPermissions().add(permission);
        roleRepository.save(role);
        
        log.info("Permission {} assigned to role {} successfully", permissionCode, roleCode);
    }

    public void revokePermissionFromRole(RoleCode roleCode, PermissionCode permissionCode, String currentUserEmail) {
        log.info("Revoking permission {} from role {} by user: {}", permissionCode, roleCode, currentUserEmail);

        if (!permissionService.hasPermission(currentUserEmail, PermissionCode.USER_ASSIGN_ROLE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Role not found"));

        Permission permission = permissionRepository.findByName(permissionCode.getCode())
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Permission not found"));

        if (!role.getPermissions().contains(permission)) {
            log.info("Permission {} not assigned to role {}", permissionCode, roleCode);
            return;
        }

        role.getPermissions().remove(permission);
        roleRepository.save(role);
        
        log.info("Permission {} revoked from role {} successfully", permissionCode, roleCode);
    }

    @Transactional(readOnly = true)
    public Map<RoleCode, List<String>> getPermissionMatrix(String currentUserEmail) {
        log.info("Fetching permission matrix by user: {}", currentUserEmail);

        if (!permissionService.hasPermission(currentUserEmail, PermissionCode.USER_ASSIGN_ROLE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        Map<RoleCode, List<String>> matrix = new HashMap<>();
        
        for (RoleCode roleCode : RoleCode.values()) {
            List<String> permissions = getRolePermissions(roleCode, currentUserEmail);
            matrix.put(roleCode, permissions);
        }
        
        return matrix;
    }

}
