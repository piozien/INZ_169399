package pl.su.su_backend.repositories.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.su.su_backend.model.budget.ClassBudget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassBudgetRepository extends JpaRepository<ClassBudget, UUID> {
    
    List<ClassBudget> findByClasses_IdOrderByYearDesc(UUID classId);
    
    Optional<ClassBudget> findByClasses_IdAndYear(UUID classId, String year);
}
