package pl.su.su_backend.controller.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.su.su_backend.dto.auth.UserPermissionsResponse;
import pl.su.su_backend.service.auth.AuthenticationService;
import pl.su.su_backend.service.auth.PermissionService;

import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Slf4j
public class PermissionController {

    private final AuthenticationService authenticationService;
    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<UserPermissionsResponse> getPermissions(
            @AuthenticationPrincipal Object principal,
            @RequestParam(required = false) UUID councilId
    ) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        UserPermissionsResponse response = permissionService.getUserPermissions(email, councilId);

        return ResponseEntity.ok(response);
    }
}
