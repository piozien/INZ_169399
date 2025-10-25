package pl.su.su_backend.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.user.LoginResponseDto;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.user.UserService;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final UsersRepository usersRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication type");
            return;
        }

        DefaultOAuth2User principal = (DefaultOAuth2User) token.getPrincipal();

        String email = firstNonNull(
                principal.getAttribute("email"),
                principal.getAttribute("mail"),
                principal.getAttribute("preferred_username")
        );
        String microsoftUserId = (String) principal.getAttribute("sub");
        String given = firstNonNull(
                principal.getAttribute("given_name"), // Open ID Connect
                principal.getAttribute("givenName")    // Microsoft Graph
        );
        String family = firstNonNull(
                principal.getAttribute("family_name"), // OIDC
                principal.getAttribute("surname")       // Microsoft Graph
        );
        String name;
        if (given != null || family != null) {
            name = ((given != null ? given : "") + (family != null ? (" " + family) : "")).trim();
        } else {
            name = firstNonNull(
                    principal.getAttribute("name"),
                    principal.getAttribute("displayName"),
                    email
            );
        }

        log.info("Microsoft OAuth2 login - Email: {}, Name: {}", email, name);

        Users existingUser = usersRepository.findByEmail(email).orElse(null);
        
        if (existingUser != null) {
            if (existingUser.getStatus() == StatusEnum.BLOCKED) {
                log.warn("Blocked user {} attempted to login via Microsoft OAuth2", email);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Account is blocked");
                return;
            }
            
            if (existingUser.getAuthProvider() == AuthProvider.LOCAL) {
                log.info("Found local account for email: {}, changing provider to Microsoft", email);
                existingUser.setAuthProvider(AuthProvider.MICROSOFT);
                existingUser.setExternalId(microsoftUserId);
                usersRepository.save(existingUser);
                log.info("Successfully changed provider to Microsoft, status preserved: {}", existingUser.getStatus());
            } else if (existingUser.getAuthProvider() == AuthProvider.MICROSOFT) {
                log.info("User {} already has Microsoft account", email);
            }
        } else {
            log.warn("User {} not found in system, cannot login via Microsoft OAuth2", email);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not found. Please register first.");
            return;
        }

        LoginResponseDto login = userService.loginOrRegisterOAuth2(email, name, null, AuthProvider.MICROSOFT);

        int accessTtl = login.getExpiresIn() != null ? Math.toIntExact(login.getExpiresIn()) : 3600;
        setCookie(response, "ACCESS_TOKEN", login.getAccessToken(), accessTtl);
        setCookie(response, "REFRESH_TOKEN", login.getRefreshToken(), 60 * 60 * 24 * 30); // 30 days

        response.sendRedirect(frontendUrl + "/oauth2/success");
    }

    private String firstNonNull(Object... values) {
        for (Object v : values) {
            if (v != null) return String.valueOf(v);
        }
        return null;
    }

    private void setCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        if (value == null) value = "";
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value)
          .append("; Path=/; HttpOnly; Secure; SameSite=Lax");
        if (maxAgeSeconds > 0) {
            sb.append("; Max-Age=").append(maxAgeSeconds);
        }
        response.addHeader("Set-Cookie", sb.toString());
    }
}


