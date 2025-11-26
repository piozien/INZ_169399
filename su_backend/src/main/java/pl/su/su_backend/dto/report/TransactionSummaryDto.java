package pl.su.su_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryDto {
    private String description;
    private BigDecimal amount;
    private String type; // "INCOME" or "EXPENSE"
    private String category;
    private LocalDate transactionDate;
    private String createdBy;
    private String payerUser;
}
