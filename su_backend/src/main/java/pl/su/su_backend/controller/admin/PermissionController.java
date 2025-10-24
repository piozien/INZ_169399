package pl.su.su_backend.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasPermission(null, 'ADMIN')")
public class PermissionController {

    private final PermissionManagementService permissionManagementService;

    @GetMapping("/roles/{roleCode}")
    public ResponseEntity<List<String>> getRolePermissions(@PathVariable RoleCode roleCode,
                                                          @AuthenticationPrincipal User principal) {
        log.info("Fetching permissions for role {} by user: {}", roleCode, principal.getUsername());
        List<String> permissions = permissionManagementService.getRolePermissions(roleCode, principal.getUsername());
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PermissionCode>> getAllPermissions(@AuthenticationPrincipal User principal) {
        log.info("Fetching all permissions by user: {}", principal.getUsername());
        List<PermissionCode> permissions = permissionManagementService.getAllPermissions(principal.getUsername());
        return ResponseEntity.ok(permissions);
    }

    @PostMapping("/assign")
    public ResponseEntity<String> assignPermissionToRole(@RequestBody PermissionAssignmentDto assignment,
                                                        @AuthenticationPrincipal User principal) {
        log.info("Assigning permission {} to role {} by user: {}", 
                assignment.getPermission(), assignment.getRoleCode(), principal.getUsername());
        permissionManagementService.assignPermissionToRole(assignment.getRoleCode(), 
                assignment.getPermission(), principal.getUsername());
        return ResponseEntity.ok("Permission assigned successfully");
    }

    @DeleteMapping("/revoke")
    public ResponseEntity<String> revokePermissionFromRole(@RequestBody PermissionAssignmentDto assignment,
                                                          @AuthenticationPrincipal User principal) {
        log.info("Revoking permission {} from role {} by user: {}", 
                assignment.getPermission(), assignment.getRoleCode(), principal.getUsername());
        permissionManagementService.revokePermissionFromRole(assignment.getRoleCode(), 
                assignment.getPermission(), principal.getUsername());
        return ResponseEntity.ok("Permission revoked successfully");
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleCode>> getAllRoles(@AuthenticationPrincipal User principal) {
        log.info("Fetching all roles by user: {}", principal.getUsername());
        List<RoleCode> roles = permissionManagementService.getAllRoles(principal.getUsername());
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/matrix")
    public ResponseEntity<Map<RoleCode, List<String>>> getPermissionMatrix(@AuthenticationPrincipal User principal) {
        log.info("Fetching permission matrix by user: {}", principal.getUsername());
        Map<RoleCode, List<String>> matrix = permissionManagementService.getPermissionMatrix(principal.getUsername());
        return ResponseEntity.ok(matrix);
    }
}
