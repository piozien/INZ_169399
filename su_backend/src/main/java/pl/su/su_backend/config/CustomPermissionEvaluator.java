// https://www.baeldung.com/spring-security-create-new-custom-security-expression - 24.10.2025; 21:00 - 25.10.2025 13:30
// https://docs.spring.io/spring-security/site/docs/4.2.5.RELEASE/apidocs/org/springframework/security/access/PermissionEvaluator.html 24.10.2025; 21:00 - 25.10.2025 13:30
package pl.su.su_backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.service.auth.PermissionService;

import java.io.Serializable;

@Slf4j
public record CustomPermissionEvaluator(PermissionService permissionService) implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        log.info("CustomPermissionEvaluator.hasPermission called with authentication: {}, targetDomainObject: {}, permission: {}",
                authentication != null ? authentication.getName() : "null", targetDomainObject, permission);

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Authentication is null or not authenticated");
            return false;
        }

        String userEmail = authentication.getName();
        String permissionString = permission.toString();

        log.info("Checking permission for user: {} permission: {}", userEmail, permissionString);

        try {
            PermissionCode permissionCode = PermissionCode.valueOf(permissionString);
            boolean result = permissionService.hasPermission(userEmail, permissionCode);
            log.info("Permission check result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error checking permission: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        log.info("CustomPermissionEvaluator.hasPermission called with authentication: {}, targetId: {}, targetType: {}, permission: {}",
                authentication != null ? authentication.getName() : "null", targetId, targetType, permission);

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Authentication is null or not authenticated");
            return false;
        }

        String userEmail = authentication.getName();
        String permissionString = permission.toString();

        log.info("Checking permission for user: {} permission: {}", userEmail, permissionString);

        try {
            PermissionCode permissionCode = PermissionCode.valueOf(permissionString);
            boolean result = permissionService.hasPermission(userEmail, permissionCode);
            log.info("Permission check result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error checking permission: {}", e.getMessage(), e);
            return false;
        }
    }
}
