package pl.su.su_backend.controller.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.report.BudgetReportDto;
import pl.su.su_backend.dto.report.ReportRequestDto;
import pl.su.su_backend.service.report.BudgetReportService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class BudgetReportController {

    private final BudgetReportService budgetReportService;

    @PostMapping("/class-budgets/{budgetId}")
    @PreAuthorize("hasPermission(null, 'CLASS_BUDGET_VIEW')")
    public ResponseEntity<BudgetReportDto> generateClassBudgetReport(@PathVariable UUID budgetId,
                                                                   @RequestBody ReportRequestDto request,
                                                                   @AuthenticationPrincipal User principal) {
        log.info("Generating class budget report for budget {} by user: {}", budgetId, principal.getUsername());
        BudgetReportDto report = budgetReportService.generateClassBudgetReport(budgetId, request, principal.getUsername());
        return ResponseEntity.ok(report);
    }


    @PostMapping("/council-budgets/{budgetId}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_BUDGET_VIEW')")
    public ResponseEntity<BudgetReportDto> generateCouncilBudgetReport(@PathVariable UUID budgetId,
                                                                     @RequestBody ReportRequestDto request,
                                                                     @AuthenticationPrincipal User principal) {
        log.info("Generating council budget report for budget {} by user: {}", budgetId, principal.getUsername());
        BudgetReportDto report = budgetReportService.generateCouncilBudgetReport(budgetId, request, principal.getUsername());
        return ResponseEntity.ok(report);
    }

    @GetMapping("/class-budgets/{budgetId}/quick")
    @PreAuthorize("hasPermission(null, 'CLASS_BUDGET_VIEW')")
    public ResponseEntity<BudgetReportDto> generateQuickClassBudgetReport(@PathVariable UUID budgetId,
                                                                        @AuthenticationPrincipal User principal) {
        log.info("Generating quick class budget report for budget {} by user: {}", budgetId, principal.getUsername());
        ReportRequestDto request = ReportRequestDto.builder()
                .includeTransactions(true)
                .reportType("SUMMARY")
                .showPayerInfo(false) // Default: don't show payer info
                .build();
        BudgetReportDto report = budgetReportService.generateClassBudgetReport(budgetId, request, principal.getUsername());
        return ResponseEntity.ok(report);
    }

    @GetMapping("/council-budgets/{budgetId}/quick")
    @PreAuthorize("hasPermission(null, 'COUNCIL_BUDGET_VIEW')")
    public ResponseEntity<BudgetReportDto> generateQuickCouncilBudgetReport(@PathVariable UUID budgetId,
                                                                          @AuthenticationPrincipal User principal) {
        log.info("Generating quick council budget report for budget {} by user: {}", budgetId, principal.getUsername());
        ReportRequestDto request = ReportRequestDto.builder()
                .includeTransactions(true)
                .reportType("SUMMARY")
                .showPayerInfo(false) // Council reports don't have payer info
                .build();
        BudgetReportDto report = budgetReportService.generateCouncilBudgetReport(budgetId, request, principal.getUsername());
        return ResponseEntity.ok(report);
    }
}
