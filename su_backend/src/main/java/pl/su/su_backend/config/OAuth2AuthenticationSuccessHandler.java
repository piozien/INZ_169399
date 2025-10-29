package pl.su.su_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import pl.su.su_backend.dto.user.LoginResponseDto;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.service.user.UserService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String externalId = oauth2User.getName(); // This is the "sub" claim from the token

        if (email == null) {
            log.error("Email not found from OAuth2 provider");
            response.sendRedirect(frontendUrl + "/oauth2/failure?error=EmailNotFound");
            return;
        }

        LoginResponseDto loginResponse = userService.loginOrRegisterOAuth2(email, name, externalId, AuthProvider.MICROSOFT);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/success")
                .queryParam("accessToken", loginResponse.getAccessToken())
                .queryParam("refreshToken", loginResponse.getRefreshToken())
                .build().toUriString();

        response.sendRedirect(redirectUrl);
    }
}


