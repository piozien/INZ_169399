package pl.su.su_backend.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.user.*;
import pl.su.su_backend.service.user.UserService;
import pl.su.su_backend.service.user.MailService;
import pl.su.su_backend.config.JwtConfig;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.repositories.user.UsersRepository;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final MailService mailService;
    private final JwtConfig jwtConfig;
    private final UsersRepository usersRepository;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto userRequestDto) {
        log.info("Registration request for email: {}", userRequestDto.getEmail());
        try {
            UserResponseDto user = userService.registerUser(userRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            log.error("Registration failed for email: {}, error: {}", userRequestDto.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("Login request for email: {}", loginRequestDto.getEmail());
        try {
            LoginResponseDto loginResponse = userService.loginUser(loginRequestDto);
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            log.error("Login failed for email: {}, error: {}", loginRequestDto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        log.info("Token refresh request");
        try {
            LoginResponseDto loginResponse = userService.refreshToken(refreshTokenRequestDto);
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam UUID userId) {
        log.info("Logout request for user ID: {}", userId);
        try {
            userService.logoutUser(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Logout failed for user ID: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/activate")
    public ResponseEntity<Void> activate(@RequestParam("token") String token) {
        try {
            if (!jwtConfig.isActivationToken(token)) {
                return ResponseEntity.badRequest().build();
            }
            String email = jwtConfig.extractEmail(token);
            var user = usersRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (user.getStatus() != StatusEnum.CONFIRMED) {
                user.setStatus(StatusEnum.CONFIRMED);
                usersRepository.save(user);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Activation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/activate/resend")
    public ResponseEntity<Void> resendActivation(@RequestParam String email) {
        try {
            var user = usersRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (user.getStatus() == StatusEnum.CONFIRMED) {
                return ResponseEntity.ok().build();
            }
            String activationToken = jwtConfig.generateActivationToken(user.getEmail());
            String activationUrl = frontendUrl + "/activate?token=" + activationToken;
            
            mailService.sendActivationEmail(user.getEmail(), user.getFullName(), activationUrl);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Resend activation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        log.info("Email check request for: {}", email);
        try {
            boolean exists = userService.userExists(email);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            log.error("Email check failed for: {}, error: {}", email, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
