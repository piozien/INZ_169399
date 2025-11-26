package pl.su.su_backend.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.auth.PasswordResetConfirmDto;
import pl.su.su_backend.dto.auth.PasswordResetRequestDto;
import pl.su.su_backend.service.auth.PasswordResetService;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto request) {
        log.info("Password reset requested for: {}", request.getEmail());

        passwordResetService.sendPasswordResetEmail(request.getEmail());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate/{token}")
    public ResponseEntity<Boolean> validateToken(@PathVariable String token) {
        boolean isValid = passwordResetService.isTokenValid(token);
        log.info("Token validation result: {}", isValid);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmDto request) {
        log.info("Confirming password reset with token");

        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        return ResponseEntity.ok().build();
    }
}