package pl.su.su_backend.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.service.admin.PermissionManagementService;
import pl.su.su_backend.service.user.UserService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final PermissionManagementService permissionManagementService;
    private final UserService userService;

    @GetMapping("/permissions/matrix")
    @PreAuthorize("hasPermission(null, 'ROLE_MANAGE')")
    public ResponseEntity<Map<RoleCode, List<String>>> getPermissionMatrix() {
        return ResponseEntity.ok(permissionManagementService.getPermissionMatrix());
    }

    @PostMapping("/roles/{roleCode}/permissions/{permissionCode}")
    @PreAuthorize("hasPermission(null, 'ROLE_MANAGE')")
    public ResponseEntity<Void> assignPermission(
            @PathVariable RoleCode roleCode,
            @PathVariable PermissionCode permissionCode,
            @AuthenticationPrincipal Object principal) {

        UUID actingUserId = getCurrentUserId(principal);
        permissionManagementService.assignPermissionToRole(roleCode, permissionCode, actingUserId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/roles/{roleCode}/permissions/{permissionCode}")
    @PreAuthorize("hasPermission(null, 'ROLE_MANAGE')")
    public ResponseEntity<Void> revokePermission(
            @PathVariable RoleCode roleCode,
            @PathVariable PermissionCode permissionCode,
            @AuthenticationPrincipal Object principal) {

        UUID actingUserId = getCurrentUserId(principal);
        permissionManagementService.revokePermissionFromRole(roleCode, permissionCode, actingUserId);

        return ResponseEntity.ok().build();
    }

    private UUID getCurrentUserId(Object principal) {
        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }
        return userService.getCurrentUserId(email);
    }
}