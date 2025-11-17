package pl.su.su_backend.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.user.UserRequestDto;
import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.service.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import pl.su.su_backend.service.user.RoleAssignmentService;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.service.auth.AuthenticationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final RoleAssignmentService roleAssignmentService;
    private final AuthenticationService authenticationService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(@AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching all users for: {}", email);
        List<UserResponseDto> users = userService.getAllUsers(email);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable UUID userId, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching roles for user {} by {}", userId, email);
        return ResponseEntity.ok(userService.getUserRoles(userId));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        UserResponseDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID userId, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching user with ID: {} for user: {}", userId, email);
        UserResponseDto user = userService.getUserById(userId, email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String pathEmail, @AuthenticationPrincipal Object principal) {
        String principalEmail = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching user with email: {} for user: {}", pathEmail, principalEmail);
        UserResponseDto user = userService.getUserByEmail(pathEmail);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<List<UserResponseDto>> getUsersByClass(@PathVariable UUID classId, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching users for class ID: {} for user: {}", classId, email);
        List<UserResponseDto> users = userService.getUsersByClass(classId);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable UUID userId,
                                                      @Valid @RequestBody UserRequestDto userRequestDto,
                                                      @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Updating user with ID: {} by user: {}", userId, email);
        UserResponseDto updatedUser = userService.updateUser(userId, userRequestDto, email);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasPermission(null, 'USER_DELETE')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Deleting user with ID: {} by user: {}", userId, email);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/block")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<UserResponseDto> blockUser(@PathVariable UUID userId, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Blocking user with ID: {} by user: {}", userId, email);
        UserResponseDto blockedUser = userService.blockUser(userId, email);
        return ResponseEntity.ok(blockedUser);
    }

    @PostMapping("/{userId}/unblock")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<UserResponseDto> unblockUser(@PathVariable UUID userId, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Unblocking user with ID: {} by user: {}", userId, email);
        UserResponseDto unblockedUser = userService.unblockUser(userId, email);
        return ResponseEntity.ok(unblockedUser);
    }

    @PostMapping("/{userId}/class/{classId}")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<UserResponseDto> assignUserToClass(@PathVariable UUID userId,
                                                             @PathVariable UUID classId,
                                                             @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Assigning user {} to class {} by {}", userId, classId, email);
        UserResponseDto updatedUser = userService.assignUserToClass(userId, classId, email);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}/class")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<UserResponseDto> removeUserFromClass(@PathVariable UUID userId, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Removing user {} from class by {}", userId, email);
        UserResponseDto updatedUser = userService.removeUserFromClass(userId, email);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{userId}/roles/{roleCode}")
    @PreAuthorize("hasPermission(null, 'ROLE_MANAGE')")
    public ResponseEntity<UserResponseDto> assignRole(@PathVariable UUID userId,
                                                      @PathVariable RoleCode roleCode,
                                                      @RequestParam(required = false, defaultValue = "Role assigned via API") String reason,
                                                      @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Assigning role {} to user {} by {}", roleCode, userId, email);
        roleAssignmentService.assignRoleByEmail(email, userId, roleCode, reason);
        UserResponseDto updatedUser = userService.getUserById(userId, email);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}/roles/{roleCode}")
    @PreAuthorize("hasPermission(null, 'ROLE_MANAGE')")
    public ResponseEntity<UserResponseDto> removeRole(@PathVariable UUID userId,
                                                      @PathVariable RoleCode roleCode,
                                                      @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Removing role {} from user {} by {}", roleCode, userId, email);
        roleAssignmentService.revokeRoleByEmail(email, userId, roleCode, "Role revoked via API");
        UserResponseDto updatedUser = userService.getUserById(userId, email);
        return ResponseEntity.ok(updatedUser);
    }
}