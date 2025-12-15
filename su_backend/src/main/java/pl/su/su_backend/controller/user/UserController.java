package pl.su.su_backend.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.user.ChangePasswordRequestDto;
import pl.su.su_backend.dto.user.UserUpdateRequestDto;
import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.service.user.RoleAssignmentService;
import pl.su.su_backend.service.user.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final RoleAssignmentService roleAssignmentService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal Object principal) {
        String email = getCurrentUserEmail(principal);
        UUID userId = userService.getCurrentUserId(email);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID userId) {
        log.info("Fetching user ID: {}", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<Users> getUserByEmail(@PathVariable("email") String targetEmail) {
        log.info("Fetching user by email: {}", targetEmail);
        return ResponseEntity.ok(userService.getUserByEmailEntity(targetEmail));
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(@AuthenticationPrincipal Object principal) {
        String email = getCurrentUserEmail(principal);
        log.info("Fetching all users by: {}", email);
        return ResponseEntity.ok(userService.getAllUsers(email));
    }

    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserRoles(userId));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable UUID userId,
                                                      @Valid @RequestBody UserUpdateRequestDto request,
                                                      @AuthenticationPrincipal Object principal) {
        String email = getCurrentUserEmail(principal);
        log.info("Updating user ID: {} by user: {}", userId, email);
        return ResponseEntity.ok(userService.updateUser(userId, request, email));
    }

    @PatchMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto request,
            @AuthenticationPrincipal Object principal) {

        String email = getCurrentUserEmail(principal);
        UUID userId = userService.getCurrentUserId(email);

        log.info("Password change request for user: {}", email);
        userService.changePassword(userId, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId,
                                           @AuthenticationPrincipal Object principal) {
        String email = getCurrentUserEmail(principal);
        log.info("Soft-deleting user ID: {} by admin: {}", userId, email);
        userService.deleteUser(userId, email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/unblock")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<UserResponseDto> unblockUser(@PathVariable UUID userId, @AuthenticationPrincipal Object principal) {
        String email = getCurrentUserEmail(principal);
        log.info("Unblocking user ID: {} by admin: {}", userId, email);
        return ResponseEntity.ok(userService.unblockUser(userId, email));
    }

    @PostMapping("/{userId}/roles/{roleCode}")
    @PreAuthorize("hasPermission(null, 'ROLE_MANAGE')")
    public ResponseEntity<UserResponseDto> assignRole(@PathVariable UUID userId,
                                                      @PathVariable RoleCode roleCode,
                                                      @RequestParam(required = false, defaultValue = "API assignment") String reason,
                                                      @AuthenticationPrincipal Object principal) {
        String email = getCurrentUserEmail(principal);
        log.info("Assigning role {} to user {} by {}", roleCode, userId, email);

        roleAssignmentService.assignRoleByEmail(email, userId, roleCode, reason);

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @DeleteMapping("/{userId}/roles/{roleCode}")
    @PreAuthorize("hasPermission(null, 'ROLE_MANAGE')")
    public ResponseEntity<UserResponseDto> removeRole(@PathVariable UUID userId,
                                                      @PathVariable RoleCode roleCode,
                                                      @AuthenticationPrincipal Object principal) {
        String email = getCurrentUserEmail(principal);
        log.info("Removing role {} from user {} by {}", roleCode, userId, email);

        roleAssignmentService.revokeRoleByEmail(email, userId, roleCode, "API revocation");

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    private String getCurrentUserEmail(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String email) {
            return email;
        }
        throw new IllegalStateException("Nieznany typ użytkownika: " + principal.getClass());
    }
}