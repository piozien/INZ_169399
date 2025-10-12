package pl.su.su_backend.controller.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.user.LoginResponseDto;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.service.user.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OAuth2Controller {

    private final UserService userService;

    @GetMapping("/microsoft/callback")
    public ResponseEntity<LoginResponseDto> microsoftCallback(@AuthenticationPrincipal OAuth2User oauth2User) {
        log.info("Microsoft OAuth2 callback received");
        
        try {
            String email = oauth2User.getAttribute("mail");
            String displayName = oauth2User.getAttribute("displayName");
            String externalId = oauth2User.getAttribute("id");
            
            if (email == null || email.isEmpty()) {
                log.error("No email found in OAuth2 user data");
                return ResponseEntity.badRequest().build();
            }

            if (userService.userExists(email)) {
                log.info("Existing user login via Microsoft OAuth2: {}", email);
                LoginResponseDto loginResponse = userService.loginOAuth2User(email);
                return ResponseEntity.ok(loginResponse);
            } else {
                log.info("New user registration via Microsoft OAuth2: {}", email);
                LoginResponseDto loginResponse = userService.registerOAuth2User(email, displayName, externalId, AuthProvider.MICROSOFT);
                return ResponseEntity.ok(loginResponse);
            }
            
        } catch (Exception e) {
            log.error("Error processing Microsoft OAuth2 callback: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getOAuth2UserInfo(@AuthenticationPrincipal OAuth2User oauth2User) {
        log.info("Getting OAuth2 user info");
        
        if (oauth2User == null) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> userInfo = Map.of(
                "name", oauth2User.getAttribute("displayName"),
                "email", oauth2User.getAttribute("mail"),
                "id", oauth2User.getAttribute("id")
        );

        return ResponseEntity.ok(userInfo);
    }
}
