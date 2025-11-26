package pl.su.su_backend.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.report.*;
import pl.su.su_backend.model.budget.CouncilBudget;
import pl.su.su_backend.model.budget.CouncilTransaction;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.TransactionType;
import pl.su.su_backend.repositories.budget.CouncilBudgetRepository;
import pl.su.su_backend.repositories.budget.CouncilTransactionRepository;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.user.UserService;
import pl.su.su_backend.exception.ApiException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetReportService {

    private final CouncilBudgetRepository councilBudgetRepository;
    private final CouncilTransactionRepository councilTransactionRepository;
    private final UserService userService;
    private final PermissionService permissionService;
    private final TransactionReportMapper transactionReportMapper;

    @Transactional(readOnly = true)
    public BudgetReportDto generateCouncilBudgetReport(UUID budgetId, ReportRequestDto request, String currentUserEmail) {

        Users user = userService.getUserByEmailEntity(currentUserEmail);

        CouncilBudget budget = councilBudgetRepository.findById(budgetId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono budżetu rady"));

        if (!permissionService.hasPermission(user.getId(), PermissionCode.REPORT_GENERATE)) {
            throw ApiException.forbidden("Brak dostępu do generowania raportów");
        }

        LocalDate fromDate = request.getFromDate() != null ? request.getFromDate() : LocalDate.now().minusMonths(1);
        LocalDate toDate = request.getToDate() != null ? request.getToDate() : LocalDate.now();
        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<CouncilTransaction> transactions = councilTransactionRepository.findByBudgetIdAndDateBetween(
                budgetId, startDateTime, endDateTime);

        return buildCouncilBudgetReport(budget, transactions, fromDate, toDate, request.isIncludeTransactions());
    }


    private BudgetReportDto buildCouncilBudgetReport(CouncilBudget budget, List<CouncilTransaction> transactions,
                                                     LocalDate fromDate, LocalDate toDate, boolean includeTransactions) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        List<TransactionSummaryDto> transactionSummaries = new ArrayList<>();
        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        Map<String, Integer> categoryCounts = new HashMap<>();

        for (CouncilTransaction transaction : transactions) {
            BigDecimal amount = transaction.getAmount();
            String category = "Brak kategorii"; // consider adding a category to transactions in the future

            if (transaction.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(amount);
                incomeByCategory.merge(category, amount, BigDecimal::add);
            } else {
                totalExpenses = totalExpenses.add(amount);
                expensesByCategory.merge(category, amount, BigDecimal::add);
            }

            categoryCounts.merge(category, 1, Integer::sum);

            if (includeTransactions) {
                transactionSummaries.add(transactionReportMapper.toSummaryDto(transaction, transaction.getAddedBy()));
            }
        }

        BigDecimal currentBalance = budget.getInitialAmount().add(totalIncome).subtract(totalExpenses);

        List<CategorySummaryDto> incomeCategories = buildCategorySummaries(incomeByCategory, totalIncome, categoryCounts);
        List<CategorySummaryDto> expenseCategories = buildCategorySummaries(expensesByCategory, totalExpenses, categoryCounts);

        return BudgetReportDto.builder()
                .budgetName("Samorząd Uczniowski - " + budget.getYear())
                .budgetType("COUNCIL")
                .initialAmount(budget.getInitialAmount())
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .currentBalance(currentBalance)
                .reportDate(LocalDate.now())
                .fromDate(fromDate)
                .toDate(toDate)
                .transactions(transactionSummaries)
                .incomeByCategory(incomeCategories)
                .expensesByCategory(expenseCategories)
                .build();
    }

    private List<CategorySummaryDto> buildCategorySummaries(Map<String, BigDecimal> categoryAmounts,
                                                            BigDecimal totalAmount, Map<String, Integer> categoryCounts) {
        List<CategorySummaryDto> summaries = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : categoryAmounts.entrySet()) {
            String category = entry.getKey();
            BigDecimal amount = entry.getValue();
            int count = categoryCounts.getOrDefault(category, 0);

            BigDecimal percentage = BigDecimal.ZERO;
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.divide(totalAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            summaries.add(CategorySummaryDto.builder()
                    .category(category)
                    .totalAmount(amount)
                    .transactionCount(count)
                    .percentage(percentage)
                    .build());
        }

        summaries.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
        return summaries;
    }
}