package pl.su.su_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetReportDto {
    private String budgetName;
    private String budgetType; // "CLASS" or "COUNCIL"
    private BigDecimal initialAmount;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal currentBalance;
    private LocalDate reportDate;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<TransactionSummaryDto> transactions;
    private List<CategorySummaryDto> incomeByCategory;
    private List<CategorySummaryDto> expensesByCategory;
}
