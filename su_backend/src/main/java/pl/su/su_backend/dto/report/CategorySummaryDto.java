package pl.su.su_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryDto {
    private String category;
    private BigDecimal totalAmount;
    private int transactionCount;
    private BigDecimal percentage;
}
