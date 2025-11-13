package pl.su.su_backend.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.user.*;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.service.user.UserService;
import pl.su.su_backend.service.user.MailService;
import pl.su.su_backend.config.JwtConfig;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
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
        UserResponseDto user = userService.registerUser(userRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("Login request for email: {}", loginRequestDto.getEmail());
        LoginResponseDto loginResponse = userService.loginUser(loginRequestDto);
        return ResponseEntity.ok(loginResponse);
    }


    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        log.info("Token refresh request");
        LoginResponseDto loginResponse = userService.refreshToken(refreshTokenRequestDto);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam UUID userId) {
        log.info("Logout request for user ID: {}", userId);
        userService.logoutUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/activate")
    public ResponseEntity<Void> activate(@RequestParam("token") String token) {
        if (!jwtConfig.isActivationToken(token)) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid activation token");
        }
        String email = jwtConfig.extractEmail(token);
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        if (user.getStatus() != StatusEnum.CONFIRMED) {
            user.setStatus(StatusEnum.CONFIRMED);
            usersRepository.save(user);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/activate/resend")
    public ResponseEntity<Void> resendActivation(@RequestParam String email) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        if (user.getStatus() == StatusEnum.CONFIRMED) {
            return ResponseEntity.ok().build();
        }
        String activationToken = jwtConfig.generateActivationToken(user.getEmail());
        String activationUrl = frontendUrl + "/activate?token=" + activationToken;
        
        mailService.sendActivationEmail(user.getEmail(), user.getFullName(), activationUrl);
        return ResponseEntity.ok().build();
    }
}
