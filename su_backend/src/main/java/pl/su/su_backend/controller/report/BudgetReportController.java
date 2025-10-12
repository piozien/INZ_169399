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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BudgetReportDto> generateClassBudgetReport(@PathVariable UUID budgetId,
                                                                   @RequestBody ReportRequestDto request,
                                                                   @AuthenticationPrincipal User principal) {
        log.info("Generating class budget report for budget {} by user: {}", budgetId, principal.getUsername());
        try {
            BudgetReportDto report = budgetReportService.generateClassBudgetReport(budgetId, request, principal.getUsername());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Failed to generate class budget report for budget {}: {}", budgetId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/class-budgets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BudgetReportDto>> generateAllClassBudgetsReport(@AuthenticationPrincipal User principal) {
        log.info("Generating all class budgets report for user: {}", principal.getUsername());
        try {
            List<BudgetReportDto> reports = budgetReportService.generateAllClassBudgetsReport(principal.getUsername());
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Failed to generate all class budgets report: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/council-budgets/{budgetId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BudgetReportDto> generateCouncilBudgetReport(@PathVariable UUID budgetId,
                                                                     @RequestBody ReportRequestDto request,
                                                                     @AuthenticationPrincipal User principal) {
        log.info("Generating council budget report for budget {} by user: {}", budgetId, principal.getUsername());
        try {
            BudgetReportDto report = budgetReportService.generateCouncilBudgetReport(budgetId, request, principal.getUsername());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Failed to generate council budget report for budget {}: {}", budgetId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/class-budgets/{budgetId}/quick")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BudgetReportDto> generateQuickClassBudgetReport(@PathVariable UUID budgetId,
                                                                        @AuthenticationPrincipal User principal) {
        log.info("Generating quick class budget report for budget {} by user: {}", budgetId, principal.getUsername());
        try {
            ReportRequestDto request = ReportRequestDto.builder()
                    .includeTransactions(true)
                    .reportType("SUMMARY")
                    .showPayerInfo(false) // Default: don't show payer info
                    .build();
            BudgetReportDto report = budgetReportService.generateClassBudgetReport(budgetId, request, principal.getUsername());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Failed to generate quick class budget report for budget {}: {}", budgetId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/council-budgets/{budgetId}/quick")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BudgetReportDto> generateQuickCouncilBudgetReport(@PathVariable UUID budgetId,
                                                                          @AuthenticationPrincipal User principal) {
        log.info("Generating quick council budget report for budget {} by user: {}", budgetId, principal.getUsername());
        try {
            ReportRequestDto request = ReportRequestDto.builder()
                    .includeTransactions(true)
                    .reportType("SUMMARY")
                    .showPayerInfo(false) // Council reports don't have payer info
                    .build();
            BudgetReportDto report = budgetReportService.generateCouncilBudgetReport(budgetId, request, principal.getUsername());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Failed to generate quick council budget report for budget {}: {}", budgetId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
