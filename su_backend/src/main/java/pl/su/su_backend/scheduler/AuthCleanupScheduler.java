package pl.su.su_backend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.su.su_backend.service.auth.PasswordResetService;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class AuthCleanupScheduler {

    private final PasswordResetService passwordResetService;


    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void cleanupExpiredPasswordResetTokens() {
        log.debug("Task started: Cleanup expired password reset tokens");
        try {
            passwordResetService.cleanupExpiredTokens();
            log.debug("Task finished: Cleanup completed");
        } catch (Exception e) {
            log.error("Task failed: Error cleaning up tokens", e);
        }
    }
}