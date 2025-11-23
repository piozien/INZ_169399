package pl.su.su_backend.repositories.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.su.su_backend.model.budget.ClassBudget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassBudgetRepository extends JpaRepository<ClassBudget, UUID> {
    
    List<ClassBudget> findByClasses_IdOrderByYearDesc(UUID classId);
    
    Optional<ClassBudget> findByClasses_IdAndYear(UUID classId, String year);
}
