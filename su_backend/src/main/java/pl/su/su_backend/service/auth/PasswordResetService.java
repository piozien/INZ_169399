package pl.su.su_backend.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.users.PasswordResetToken;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.auth.PasswordResetTokenRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.user.MailService;
import pl.su.su_backend.service.user.UserService;

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
    private final UserService userService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${PASSWORD_RESET_EXPIRATION_HOURS}")
    private int expirationHours;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String email) {
        log.info("Request password reset for: {}", email);

        Users user = userService.getUserByEmailEntity(email);

        if (user.isBlocked()) {
            throw ApiException.forbidden("Twoje konto jest zablokowane. Nie możesz zresetować hasła.");
        }

        tokenRepository.deleteByUser_Id(user.getId());

        String token = generateSecureToken();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(expirationHours))
                .build();

        tokenRepository.save(resetToken);

        String resetUrl = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/reset-password")
                .query("token={token}")
                .buildAndExpand(token)
                .toUriString();

        mailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetUrl);

        log.info("Reset email sent to: {}", email);
    }

    public void resetPassword(String token, String newPassword) {
        log.info("Attempt to reset password with token...");

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Nieprawidłowy lub przestarzały token"));

        if (!resetToken.isValid()) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Token wygasł lub został już użyty");
        }

        Users user = resetToken.getUser();

        if (StatusEnum.BLOCKED.equals(user.getStatus())) {
            throw ApiException.forbidden("Konto użytkownika jest zablokowane.");
        }
        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        usersRepository.save(user);

        resetToken.markAsUsed();
        tokenRepository.save(resetToken);

        log.info("The password has been successfully changed for: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public boolean isTokenValid(String token) {
        return tokenRepository.findByToken(token)
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }

    public void cleanupExpiredTokens() {
        log.info("Cleaning expired password reset tokens...");
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}