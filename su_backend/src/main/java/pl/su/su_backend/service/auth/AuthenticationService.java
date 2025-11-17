package pl.su.su_backend.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticationService {

    public String getEmailFromPrincipal(Object principal) {
        switch (principal) {
            case User user -> {
                return user.getUsername();
            }
            case OAuth2User oAuth2User -> {
                return oAuth2User.getAttribute("email");
            }
            case null -> {
                log.warn("Principal is null. This should only happen for public endpoints.");
                return null;
            }
            default -> {
                log.error("Unknown principal type: {}", principal.getClass().getName());
                throw new IllegalArgumentException("Unknown principal type");
            }
        }
    }
}
