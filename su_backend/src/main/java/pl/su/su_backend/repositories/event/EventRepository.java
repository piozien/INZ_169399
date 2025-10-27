package pl.su.su_backend.repositories.event;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.su.su_backend.model.event.Event;
import pl.su.su_backend.model.enums.EventStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByStartDateBetweenOrderByStartDateAsc(LocalDateTime startDate, LocalDateTime endDate);

    List<Event> findByStatusOrderByStartDateAsc(EventStatus status);

    List<Event> findByStatusOrderByCreatedAtDesc(EventStatus status);
    
    List<Event> findByStatusAndEndDateGreaterThanOrderByStartDateAsc(EventStatus status, LocalDateTime endDate);
    
    List<Event> findAllByOrderByStartDateAsc();
}
