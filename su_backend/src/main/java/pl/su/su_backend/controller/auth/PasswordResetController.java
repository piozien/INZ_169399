package pl.su.su_backend.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.user.PasswordResetConfirmDto;
import pl.su.su_backend.dto.user.PasswordResetRequestDto;
import pl.su.su_backend.service.auth.PasswordResetService;

@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;


    @PostMapping("/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto request) {
        log.info("Password reset request for email: {}", request.getEmail());
        
        try {
            passwordResetService.sendPasswordResetEmail(request.getEmail());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to send password reset email for: {}, error: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.ok().build();
        }
    }

    @GetMapping("/validate/{token}")
    public ResponseEntity<Boolean> validateToken(@PathVariable String token) {
        log.info("Validating password reset token");
        
        try {
            boolean isValid = passwordResetService.isTokenValid(token);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmDto request) {
        log.info("Confirming password reset");
        
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to reset password: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
