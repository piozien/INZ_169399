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
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;
import pl.su.su_backend.model.enums.StatusEnum;
import org.apache.commons.codec.digest.DigestUtils;


import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.regex.Pattern;

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
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));

        if (user.isBlocked()) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
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
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid or expired token"));

        if (!resetToken.isValid()) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Token expired or used");
        }

        Users user = resetToken.getUser();

        if (StatusEnum.BLOCKED.equals(user.getStatus())) {
            log.warn("Blocked user attempted password reset: {}", user.getEmail());
            throw ApiException.forbidden(ErrorCode.USER_BLOCKED, "User account is blocked");
        }

        user.setPassword(passwordEncoder.encode(normalizeClientSecret(newPassword)));
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

    private static final Pattern SHA256_PATTERN = Pattern.compile("^[0-9a-fA-F]{64}$");

    private String normalizeClientSecret(String candidate) {
        if (candidate == null) {
            return null;
        }
        String trimmed = candidate.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        if (SHA256_PATTERN.matcher(trimmed).matches()) {
            return trimmed.toLowerCase();
        }
        return DigestUtils.sha256Hex(trimmed);
    }
}
