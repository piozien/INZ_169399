package pl.su.su_backend.repositories.suggestion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.su.su_backend.model.enums.SuggestionStatus;
import pl.su.su_backend.model.suggestion.Suggestion;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, UUID> {

    List<Suggestion> findByCouncil_IdOrderByCreatedAtDesc(UUID councilId);

    List<Suggestion> findByUser_Id(UUID userId);

    long countByUserId(UUID userId);

    long countByUserIdAndStatus(UUID userId, SuggestionStatus suggestionStatus);

    long countByCouncilIdAndStatus(UUID id, SuggestionStatus suggestionStatus);
}
