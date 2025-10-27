package pl.su.su_backend.repositories.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.su.su_backend.model.budget.CouncilBudget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouncilBudgetRepository extends JpaRepository<CouncilBudget, UUID> {
    
    List<CouncilBudget> findByCouncil_IdOrderByYearDesc(UUID councilId);
    
    Optional<CouncilBudget> findByCouncil_IdAndYear(UUID councilId, String year);
}
