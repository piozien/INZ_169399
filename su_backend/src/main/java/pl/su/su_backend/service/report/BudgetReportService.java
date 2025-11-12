package pl.su.su_backend.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.report.*;
import pl.su.su_backend.model.budget.ClassBudget;
import pl.su.su_backend.model.budget.ClassTransaction;
import pl.su.su_backend.model.budget.CouncilBudget;
import pl.su.su_backend.model.budget.CouncilTransaction;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.TransactionType;
import pl.su.su_backend.repositories.budget.ClassBudgetRepository;
import pl.su.su_backend.repositories.budget.ClassTransactionRepository;
import pl.su.su_backend.repositories.budget.CouncilBudgetRepository;
import pl.su.su_backend.repositories.budget.CouncilTransactionRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetReportService {

    private final ClassBudgetRepository classBudgetRepository;
    private final ClassTransactionRepository classTransactionRepository;
    private final CouncilBudgetRepository councilBudgetRepository;
    private final CouncilTransactionRepository councilTransactionRepository;
    private final UsersRepository usersRepository;
    private final PermissionService permissionService;

    @Transactional(readOnly = true)
    public BudgetReportDto generateClassBudgetReport(UUID budgetId, ReportRequestDto request, String currentUserEmail) {
        log.info("Generating class budget report for budget {} by user: {}", budgetId, currentUserEmail);
        
        ClassBudget budget = classBudgetRepository.findById(budgetId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Class budget not found"));
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!hasAccessToClassBudget(user, budget)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        LocalDate fromDate = request.getFromDate() != null ? request.getFromDate() : LocalDate.now().minusMonths(1);
        LocalDate toDate = request.getToDate() != null ? request.getToDate() : LocalDate.now();
        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.plusDays(1).atStartOfDay().minusNanos(1);
        
        List<ClassTransaction> transactions = classTransactionRepository.findByBudgetIdAndDateBetween(
                budgetId, startDateTime, endDateTime);
        
        return buildClassBudgetReport(budget, transactions, fromDate, toDate, request.isIncludeTransactions(), request.isShowPayerInfo());
    }

    @Transactional(readOnly = true)
    public BudgetReportDto generateCouncilBudgetReport(UUID budgetId, ReportRequestDto request, String currentUserEmail) {
        log.info("Generating council budget report for budget {} by user: {}", budgetId, currentUserEmail);
        
        CouncilBudget budget = councilBudgetRepository.findById(budgetId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Council budget not found"));
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.REPORT_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        LocalDate fromDate = request.getFromDate() != null ? request.getFromDate() : LocalDate.now().minusMonths(1);
        LocalDate toDate = request.getToDate() != null ? request.getToDate() : LocalDate.now();
        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.plusDays(1).atStartOfDay().minusNanos(1);
        
        List<CouncilTransaction> transactions = councilTransactionRepository.findByBudgetIdAndDateBetween(
                budgetId, startDateTime, endDateTime);
        
        return buildCouncilBudgetReport(budget, transactions, fromDate, toDate, request.isIncludeTransactions());
    }


    private BudgetReportDto buildClassBudgetReport(ClassBudget budget, List<ClassTransaction> transactions, 
                                                 LocalDate fromDate, LocalDate toDate, boolean includeTransactions, boolean showPayerInfo) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        
        List<TransactionSummaryDto> transactionSummaries = new ArrayList<>();
        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        Map<String, Integer> categoryCounts = new HashMap<>();
        
         for (ClassTransaction transaction : transactions) {
             BigDecimal amount = transaction.getAmount();
             String category = "No Category";
             
             if (transaction.getType() == TransactionType.INCOME) {
                 totalIncome = totalIncome.add(amount);
                 incomeByCategory.merge(category, amount, BigDecimal::add);
             } else {
                 totalExpenses = totalExpenses.add(amount);
                 expensesByCategory.merge(category, amount, BigDecimal::add);
             }
             
             categoryCounts.merge(category, 1, Integer::sum);
             
             if (includeTransactions) {
                 Users createdBy = transaction.getAddedBy();
                 
                 String payerInfo = null;
                 if (showPayerInfo && transaction.getPayerUser() != null) {
                     payerInfo = transaction.getPayerUser().getFullName();
                 }
                 
                 transactionSummaries.add(TransactionSummaryDto.builder()
                         .description(transaction.getDescription())
                         .amount(amount)
                         .type(transaction.getType().name())
                         .category(category)
                         .transactionDate(transaction.getDate().toLocalDate())
                         .createdBy(createdBy.getFullName())
                         .payerUser(payerInfo)
                         .build());
             }
         }
        
        BigDecimal currentBalance = budget.getInitialAmount().add(totalIncome).subtract(totalExpenses);
        
        List<CategorySummaryDto> incomeCategories = buildCategorySummaries(incomeByCategory, totalIncome, categoryCounts);
        List<CategorySummaryDto> expenseCategories = buildCategorySummaries(expensesByCategory, totalExpenses, categoryCounts);
        
         return BudgetReportDto.builder()
                 .budgetName(budget.getClasses().getName() + " - " + budget.getYear())
                 .budgetType("CLASS")
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
             String category = "Brak kategorii";
             
             if (transaction.getType() == TransactionType.INCOME) {
                 totalIncome = totalIncome.add(amount);
                 incomeByCategory.merge(category, amount, BigDecimal::add);
             } else {
                 totalExpenses = totalExpenses.add(amount);
                 expensesByCategory.merge(category, amount, BigDecimal::add);
             }
             
             categoryCounts.merge(category, 1, Integer::sum);
             
             if (includeTransactions) {
                 Users createdBy = transaction.getAddedBy();
                 
                 transactionSummaries.add(TransactionSummaryDto.builder()
                         .description(transaction.getDescription())
                         .amount(amount)
                         .type(transaction.getType().name())
                         .category(category)
                         .transactionDate(transaction.getDate().toLocalDate())
                         .createdBy(createdBy.getFullName())
                         .payerUser(null)
                         .build());
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

    private boolean hasAccessToClassBudget(Users user, ClassBudget budget) {
        if (!permissionService.hasPermission(user.getId(), PermissionCode.CLASS_BUDGET_VIEW)) {
            return false;
        }
        
        if (permissionService.hasPermission(user.getId(), PermissionCode.REPORT_GENERATE)) {
            return true;
        }
        
        return user.getClasses() != null && user.getClasses().getId().equals(budget.getClasses().getId());
    }

}
