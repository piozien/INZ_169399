package pl.su.su_backend.repositories.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import pl.su.su_backend.model.event.EventParticipant;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, EventParticipant.Id> {
    
    List<EventParticipant> findByEvent_Id(UUID eventId);
    
    List<EventParticipant> findByUser_Id(UUID userId);

    @Modifying
    void deleteByEvent_IdAndUser_Id(UUID eventId, UUID userId);
    
    boolean existsByEvent_IdAndUser_Id(UUID eventId, UUID userId);
}
