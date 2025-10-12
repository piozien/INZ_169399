package pl.su.su_backend.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.admin.PermissionAssignmentDto;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.service.admin.PermissionManagementService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PermissionController {

    private final PermissionManagementService permissionManagementService;

    @GetMapping("/roles/{roleCode}")
    public ResponseEntity<List<PermissionCode>> getRolePermissions(@PathVariable RoleCode roleCode,
                                                                  @AuthenticationPrincipal User principal) {
        log.info("Fetching permissions for role {} by user: {}", roleCode, principal.getUsername());
        try {
            List<PermissionCode> permissions = permissionManagementService.getRolePermissions(roleCode, principal.getUsername());
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            log.error("Failed to fetch permissions for role {}: {}", roleCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<PermissionCode>> getAllPermissions(@AuthenticationPrincipal User principal) {
        log.info("Fetching all permissions by user: {}", principal.getUsername());
        try {
            List<PermissionCode> permissions = permissionManagementService.getAllPermissions(principal.getUsername());
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            log.error("Failed to fetch all permissions: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<String> assignPermissionToRole(@RequestBody PermissionAssignmentDto assignment,
                                                        @AuthenticationPrincipal User principal) {
        log.info("Assigning permission {} to role {} by user: {}", 
                assignment.getPermission(), assignment.getRoleCode(), principal.getUsername());
        try {
            permissionManagementService.assignPermissionToRole(assignment.getRoleCode(), 
                    assignment.getPermission(), principal.getUsername());
            return ResponseEntity.ok("Permission assigned successfully");
        } catch (Exception e) {
            log.error("Failed to assign permission: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/revoke")
    public ResponseEntity<String> revokePermissionFromRole(@RequestBody PermissionAssignmentDto assignment,
                                                          @AuthenticationPrincipal User principal) {
        log.info("Revoking permission {} from role {} by user: {}", 
                assignment.getPermission(), assignment.getRoleCode(), principal.getUsername());
        try {
            permissionManagementService.revokePermissionFromRole(assignment.getRoleCode(), 
                    assignment.getPermission(), principal.getUsername());
            return ResponseEntity.ok("Permission revoked successfully");
        } catch (Exception e) {
            log.error("Failed to revoke permission: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleCode>> getAllRoles(@AuthenticationPrincipal User principal) {
        log.info("Fetching all roles by user: {}", principal.getUsername());
        try {
            List<RoleCode> roles = permissionManagementService.getAllRoles(principal.getUsername());
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            log.error("Failed to fetch all roles: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/matrix")
    public ResponseEntity<Map<RoleCode, List<PermissionCode>>> getPermissionMatrix(@AuthenticationPrincipal User principal) {
        log.info("Fetching permission matrix by user: {}", principal.getUsername());
        try {
            Map<RoleCode, List<PermissionCode>> matrix = permissionManagementService.getPermissionMatrix(principal.getUsername());
            return ResponseEntity.ok(matrix);
        } catch (Exception e) {
            log.error("Failed to fetch permission matrix: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
