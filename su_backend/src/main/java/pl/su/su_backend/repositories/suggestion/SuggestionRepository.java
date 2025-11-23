package pl.su.su_backend.repositories.suggestion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.su.su_backend.model.suggestion.Suggestion;

import java.util.List;
import java.util.UUID;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, UUID> {
    
    List<Suggestion> findAllByOrderByCreatedAtDesc();

    List<Suggestion> findByUser_IdOrderByCreatedAtDesc(UUID userId);

}
