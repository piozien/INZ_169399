package pl.su.su_backend.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final RoleAssignmentService roleAssignmentService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(@AuthenticationPrincipal User principal) {
        log.info("Fetching all users for: {}", principal.getUsername());
        try {
            List<UserResponseDto> users = userService.getAllUsers(principal.getUsername());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Failed to fetch users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{userId}/roles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable UUID userId, @AuthenticationPrincipal User principal) {
        log.info("Fetching roles for user {} by {}", userId, principal.getUsername());
        try {
            return ResponseEntity.ok(userService.getUserRoles(userId));
        } catch (Exception e) {
            log.error("Failed to get roles for user {}: {}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal User principal) {
        try {
            UserResponseDto user = userService.getUserByEmail(principal.getUsername());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to get current user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID userId, @AuthenticationPrincipal User principal) {
        log.info("Fetching user with ID: {} for user: {}", userId, principal.getUsername());
        try {
            UserResponseDto user = userService.getUserById(userId, principal.getUsername());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to fetch user with ID: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String email, @AuthenticationPrincipal User principal) {
        log.info("Fetching user with email: {} for user: {}", email, principal.getUsername());
        try {
            UserResponseDto user = userService.getUserByEmail(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Failed to fetch user with email: {}, error: {}", email, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponseDto>> getUsersByClass(@PathVariable UUID classId, @AuthenticationPrincipal User principal) {
        log.info("Fetching users for class ID: {} for user: {}", classId, principal.getUsername());
        try {
            List<UserResponseDto> users = userService.getUsersByClass(classId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Failed to fetch users for class {}: {}", classId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable UUID userId, 
                                                      @Valid @RequestBody UserRequestDto userRequestDto,
                                                      @AuthenticationPrincipal User principal) {
        log.info("Updating user with ID: {} by user: {}", userId, principal.getUsername());
        try {
            UserResponseDto updatedUser = userService.updateUser(userId, userRequestDto, principal.getUsername());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Failed to update user with ID: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId, @AuthenticationPrincipal User principal) {
        log.info("Deleting user with ID: {} by user: {}", userId, principal.getUsername());
        try {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete user with ID: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/block")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> blockUser(@PathVariable UUID userId, @AuthenticationPrincipal User principal) {
        log.info("Blocking user with ID: {} by user: {}", userId, principal.getUsername());
        try {
            UserResponseDto blockedUser = userService.blockUser(userId, principal.getUsername());
            return ResponseEntity.ok(blockedUser);
        } catch (Exception e) {
            log.error("Failed to block user with ID: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/unblock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> unblockUser(@PathVariable UUID userId, @AuthenticationPrincipal User principal) {
        log.info("Unblocking user with ID: {} by user: {}", userId, principal.getUsername());
        try {
            UserResponseDto unblockedUser = userService.unblockUser(userId, principal.getUsername());
            return ResponseEntity.ok(unblockedUser);
        } catch (Exception e) {
            log.error("Failed to unblock user with ID: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/class/{classId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> assignUserToClass(@PathVariable UUID userId,
                                                             @PathVariable UUID classId,
                                                             @AuthenticationPrincipal User principal) {
        log.info("Assigning user {} to class {} by {}", userId, classId, principal.getUsername());
        try {
            UserResponseDto updatedUser = userService.assignUserToClass(userId, classId, principal.getUsername());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Failed to assign user {} to class {}: {}", userId, classId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{userId}/class")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> removeUserFromClass(@PathVariable UUID userId, @AuthenticationPrincipal User principal) {
        log.info("Removing user {} from class by {}", userId, principal.getUsername());
        try {
            UserResponseDto updatedUser = userService.removeUserFromClass(userId, principal.getUsername());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Failed to remove user {} from class: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/roles/{roleCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> assignRole(@PathVariable UUID userId, 
                                                      @PathVariable RoleCode roleCode,
                                                      @RequestParam(required = false, defaultValue = "Role assigned via API") String reason,
                                                      @AuthenticationPrincipal User principal) {
        log.info("Assigning role {} to user {} by {}", roleCode, userId, principal.getUsername());
        try {
            roleAssignmentService.assignRoleByEmail(principal.getUsername(), userId, roleCode, reason);
            UserResponseDto updatedUser = userService.getUserById(userId, principal.getUsername());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Failed to assign role {} to user {}: {}", roleCode, userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{userId}/roles/{roleCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> removeRole(@PathVariable UUID userId, 
                                                      @PathVariable RoleCode roleCode,
                                                      @AuthenticationPrincipal User principal) {
        log.info("Removing role {} from user {} by {}", roleCode, userId, principal.getUsername());
        try {
            roleAssignmentService.revokeRoleByEmail(principal.getUsername(), userId, roleCode, "Role revoked via API");
            UserResponseDto updatedUser = userService.getUserById(userId, principal.getUsername());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Failed to remove role {} from user {}: {}", roleCode, userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}