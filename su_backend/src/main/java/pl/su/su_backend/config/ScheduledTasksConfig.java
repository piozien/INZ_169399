package pl.su.su_backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import pl.su.su_backend.service.auth.PasswordResetService;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = false)
public class ScheduledTasksConfig {

    private final PasswordResetService passwordResetService;

    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupExpiredPasswordResetTokens() {
        log.debug("Running scheduled cleanup of expired password reset tokens");
        try {
            passwordResetService.cleanupExpiredTokens();
            log.debug("Successfully cleaned up expired password reset tokens");
        } catch (Exception e) {
            log.error("Error during scheduled cleanup of password reset tokens: {}", e.getMessage());
        }
    }
}
