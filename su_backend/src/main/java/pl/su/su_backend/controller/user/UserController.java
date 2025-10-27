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
import org.springframework.security.core.userdetails.User;
import pl.su.su_backend.service.user.RoleAssignmentService;
import pl.su.su_backend.model.enums.RoleCode;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final RoleAssignmentService roleAssignmentService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(@AuthenticationPrincipal User principal) {
        log.info("Fetching all users for: {}", principal.getUsername());
        List<UserResponseDto> users = userService.getAllUsers(principal.getUsername());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable UUID userId, @AuthenticationPrincipal User principal) {
        log.info("Fetching roles for user {} by {}", userId, principal.getUsername());
        return ResponseEntity.ok(userService.getUserRoles(userId));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal User principal) {
        UserResponseDto user = userService.getUserByEmail(principal.getUsername());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID userId, @AuthenticationPrincipal User principal) {
        log.info("Fetching user with ID: {} for user: {}", userId, principal.getUsername());
        UserResponseDto user = userService.getUserById(userId, principal.getUsername());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String email, @AuthenticationPrincipal User principal) {
        log.info("Fetching user with email: {} for user: {}", email, principal.getUsername());
        UserResponseDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasPermission(null, 'USER_VIEW')")
    public ResponseEntity<List<UserResponseDto>> getUsersByClass(@PathVariable UUID classId, @AuthenticationPrincipal User principal) {
        log.info("Fetching users for class ID: {} for user: {}", classId, principal.getUsername());
        List<UserResponseDto> users = userService.getUsersByClass(classId);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable UUID userId, 
                                                      @Valid @RequestBody UserRequestDto userRequestDto,
                                                      @AuthenticationPrincipal User principal) {
        log.info("Updating user with ID: {} by user: {}", userId, principal.getUsername());
        UserResponseDto updatedUser = userService.updateUser(userId, userRequestDto, principal.getUsername());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasPermission(null, 'USER_DELETE')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId, @AuthenticationPrincipal User principal) {
        log.info("Deleting user with ID: {} by user: {}", userId, principal.getUsername());
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/block")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<UserResponseDto> blockUser(@PathVariable UUID userId, @AuthenticationPrincipal User principal) {
        log.info("Blocking user with ID: {} by user: {}", userId, principal.getUsername());
        UserResponseDto blockedUser = userService.blockUser(userId, principal.getUsername());
        return ResponseEntity.ok(blockedUser);
    }

    @PostMapping("/{userId}/unblock")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<UserResponseDto> unblockUser(@PathVariable UUID userId, @AuthenticationPrincipal User principal) {
        log.info("Unblocking user with ID: {} by user: {}", userId, principal.getUsername());
        UserResponseDto unblockedUser = userService.unblockUser(userId, principal.getUsername());
        return ResponseEntity.ok(unblockedUser);
    }

    @PostMapping("/{userId}/class/{classId}")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<UserResponseDto> assignUserToClass(@PathVariable UUID userId,
                                                             @PathVariable UUID classId,
                                                             @AuthenticationPrincipal User principal) {
        log.info("Assigning user {} to class {} by {}", userId, classId, principal.getUsername());
        UserResponseDto updatedUser = userService.assignUserToClass(userId, classId, principal.getUsername());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}/class")
    @PreAuthorize("hasPermission(null, 'USER_EDIT')")
    public ResponseEntity<UserResponseDto> removeUserFromClass(@PathVariable UUID userId, @AuthenticationPrincipal User principal) {
        log.info("Removing user {} from class by {}", userId, principal.getUsername());
        UserResponseDto updatedUser = userService.removeUserFromClass(userId, principal.getUsername());
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{userId}/roles/{roleCode}")
    @PreAuthorize("hasPermission(null, 'ROLE_MANAGE')")
    public ResponseEntity<UserResponseDto> assignRole(@PathVariable UUID userId, 
                                                      @PathVariable RoleCode roleCode,
                                                      @RequestParam(required = false, defaultValue = "Role assigned via API") String reason,
                                                      @AuthenticationPrincipal User principal) {
        log.info("Assigning role {} to user {} by {}", roleCode, userId, principal.getUsername());
        roleAssignmentService.assignRoleByEmail(principal.getUsername(), userId, roleCode, reason);
        UserResponseDto updatedUser = userService.getUserById(userId, principal.getUsername());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}/roles/{roleCode}")
    @PreAuthorize("hasPermission(null, 'ROLE_MANAGE')")
    public ResponseEntity<UserResponseDto> removeRole(@PathVariable UUID userId, 
                                                      @PathVariable RoleCode roleCode,
                                                      @AuthenticationPrincipal User principal) {
        log.info("Removing role {} from user {} by {}", roleCode, userId, principal.getUsername());
        roleAssignmentService.revokeRoleByEmail(principal.getUsername(), userId, roleCode, "Role revoked via API");
        UserResponseDto updatedUser = userService.getUserById(userId, principal.getUsername());
        return ResponseEntity.ok(updatedUser);
    }
}