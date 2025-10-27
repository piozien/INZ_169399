package pl.su.su_backend.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassTransactionResponseDto {
    
    private UUID id;
    private UUID budgetId;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime date;
    private UUID addedById;
    private String addedByFullName;
    private UUID payerUserId;
    private String payerUserFullName;
}