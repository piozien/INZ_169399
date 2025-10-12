package pl.su.su_backend.repositories.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.su.su_backend.model.budget.ClassTransaction;
import pl.su.su_backend.model.enums.TransactionType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ClassTransactionRepository extends JpaRepository<ClassTransaction, UUID> {
    
    List<ClassTransaction> findByBudget_IdOrderByDateDesc(UUID budgetId);
    
    List<ClassTransaction> findByBudget_Classes_IdOrderByDateDesc(UUID classId);
    
    List<ClassTransaction> findByTypeOrderByDateDesc(TransactionType type);
    
    List<ClassTransaction> findByPayerUser_IdOrderByDateDesc(UUID userId);
    
    List<ClassTransaction> findByDateBetweenOrderByDateDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    List<ClassTransaction> findByConfirmedFalseOrderByDateDesc();
    
    List<ClassTransaction> findByBudgetIdAndDateBetween(UUID budgetId, LocalDate startDate, LocalDate endDate);
}
