package pl.su.su_backend.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.model.users.PasswordResetToken;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.auth.PasswordResetTokenRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.user.MailService;


import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UsersRepository usersRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.expiration-hours:24}")
    private int expirationHours;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;


    public void sendPasswordResetEmail(String email) {
        log.info("Sending password reset email to: {}", email);
        
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.isBlocked()) {
            throw new RuntimeException("User account is blocked");
        }

        tokenRepository.deleteByUser_Id(user.getId());

        String token = generateSecureToken();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(expirationHours))
                .build();

        tokenRepository.save(resetToken);

        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        mailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetUrl);
        
        log.info("Password reset email sent successfully to: {}", email);
    }

    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token");
        
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (!resetToken.isValid()) {
            throw new RuntimeException("Token has expired or has already been used");
        }

        Users user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        usersRepository.save(user);

        resetToken.markAsUsed();
        tokenRepository.save(resetToken);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public boolean isTokenValid(String token) {
        return tokenRepository.findByToken(token)
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }

    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired password reset tokens");
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
