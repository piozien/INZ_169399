package pl.su.su_backend.repositories.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import pl.su.su_backend.model.users.PasswordResetToken;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    void deleteByExpiresAtBefore(LocalDateTime now);

    @Modifying
    void deleteByUser_Id(UUID userId);
}
