package pl.su.su_backend.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.user.UsersRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionService {

    private final UsersRepository usersRepository;

    public boolean hasPermission(UUID userId, PermissionCode permission) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found: " + userId));

        log.info("User {} has {} roles", user.getEmail(), user.getUserRoles().size());
        
        boolean result = user.getUserRoles().stream()
                .anyMatch(userRole -> {
                    log.info("Checking role: {} with {} permissions", 
                            userRole.getRole().getRoleCode(), 
                            userRole.getRole().getPermissions().size());
                    
                    return userRole.getRole().getPermissions().stream()
                            .anyMatch(permissionEntity -> {
                                log.info("Permission entity: {} vs required: {}", 
                                        permissionEntity.getName(), permission.getCode());
                                return permissionEntity.getName().equals(permission.getCode());
                            });
                });
        
        log.info("Final permission result: {}", result);
        return result;
    }

    public boolean hasPermission(String userEmail, PermissionCode permission) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found: " + userEmail));

        log.info("Checking permission for user: {} permission: {}", userEmail, permission);
        boolean result = hasPermission(user.getId(), permission);
        log.info("Permission result: {}", result);
        return result;
    }

    public boolean canAccessClassBudget(UUID userId, UUID classId, PermissionCode permission) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found: " + userId));

        if (!hasPermission(userId, permission)) {
            return false;
        }

        return user.getUserRoles().stream()
                .anyMatch(userRole -> {
                    // Admin roles can access any class
                    if (userRole.getRole().getRoleCode().name().contains("ADMINISTRATOR") ||
                        userRole.getRole().getRoleCode().name().contains("DYREKTOR") ||
                        userRole.getRole().getRoleCode().name().contains("OPIEKUN_SU")) {
                        return true;
                    }
                    
                    if (userRole.getRole().getRoleCode().name().contains("KLASY") &&
                        user.getClasses() != null &&
                        user.getClasses().getId().equals(classId)) {
                        return true;
                    }
                    
                    return userRole.getRole().getRoleCode().name().equals("NAUCZYCIEL");
                });
    }
}
