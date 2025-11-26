// https://www.baeldung.com/spring-security-create-new-custom-security-expression - 24.10.2025; 21:00 - 25.10.2025 13:30
// https://docs.spring.io/spring-security/site/docs/4.2.5.RELEASE/apidocs/org/springframework/security/access/PermissionEvaluator.html 24.10.2025; 21:00 - 25.10.2025 13:30
package pl.su.su_backend.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.service.auth.PermissionService;

import java.io.Serializable;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final PermissionService permissionService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String userEmail = getEmailFromPrincipal(authentication.getPrincipal());
        String permissionString = permission.toString();

        try {
            PermissionCode permissionCode = PermissionCode.valueOf(permissionString);
            return permissionService.hasPermission(userEmail, permissionCode);

        } catch (IllegalArgumentException e) {
            log.error("Unknown permission (not in Enum): {}", permissionString);
            return false;
        } catch (Exception e) {
            log.error("Error while checking permissions for {}: {}", userEmail, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, null, permission);
    }

    private String getEmailFromPrincipal(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String email) {
            return email;
        }
        log.warn("Unknown principal type: {}", principal != null ? principal.getClass().getName() : "null");
        return null;
    }
}