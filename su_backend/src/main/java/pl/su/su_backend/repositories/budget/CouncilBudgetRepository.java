package pl.su.su_backend.repositories.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.su.su_backend.model.budget.CouncilBudget;

import java.util.UUID;

@Repository
public interface CouncilBudgetRepository extends JpaRepository<CouncilBudget, UUID> {

}
