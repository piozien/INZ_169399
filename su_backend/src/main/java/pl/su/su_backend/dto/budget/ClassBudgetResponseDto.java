package pl.su.su_backend.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassBudgetResponseDto {
    
    private UUID id;
    private UUID classId;
    private String className;
    private String year;
    private BigDecimal initialAmount;
    private BigDecimal balance;
    private UUID createdById;
    private String createdByFullName;
    private LocalDateTime createdAt;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
}