package pl.su.su_backend.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.user.UsersRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final UsersRepository usersRepository;

    public boolean hasPermission(UUID userId, PermissionCode permission) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getPermissions().stream()
                        .anyMatch(permissionEntity -> permissionEntity.getName().equals(permission.getCode())));
    }

    public boolean hasPermission(String userEmail, PermissionCode permission) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        return hasPermission(user.getId(), permission);
    }

    public boolean canAccessClassBudget(UUID userId, UUID classId, PermissionCode permission) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

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
