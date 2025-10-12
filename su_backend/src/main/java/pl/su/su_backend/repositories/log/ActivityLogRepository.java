package pl.su.su_backend.repositories.log;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.su.su_backend.model.log.ActivityLog;

import java.util.List;
import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    List<ActivityLog> findByUser_IdOrderByCreatedAtDesc(UUID userId);
}


