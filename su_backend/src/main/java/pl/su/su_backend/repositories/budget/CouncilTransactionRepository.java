package pl.su.su_backend.repositories.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.su.su_backend.model.budget.CouncilTransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CouncilTransactionRepository extends JpaRepository<CouncilTransaction, UUID> {
    List<CouncilTransaction> findByBudgetId(UUID budgetId);
    
    List<CouncilTransaction> findByBudgetIdAndDateBetween(UUID budgetId, LocalDateTime startDate, LocalDateTime endDate);

}
