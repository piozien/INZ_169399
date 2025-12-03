package pl.su.su_backend.controller.council;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.budget.*;
import pl.su.su_backend.service.budget.CouncilBudgetService;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/councils")
@RequiredArgsConstructor
@Slf4j
public class CouncilBudgetController {

    private final CouncilBudgetService budgetService;

    @GetMapping("/{councilId}/budget")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilBudgetResponseDto> getBudget(@PathVariable UUID councilId, @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(budgetService.getBudget(councilId, getEmail(principal)));
    }

    @PostMapping("/{councilId}/budget")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilBudgetResponseDto> createBudget(@PathVariable UUID councilId, @Valid @RequestBody CouncilBudgetRequestDto dto, @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(budgetService.createBudget(councilId, dto, getEmail(principal)));
    }

    @PutMapping("/budget/{budgetId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilBudgetResponseDto> updateBudget(@PathVariable UUID budgetId, @Valid @RequestBody CouncilBudgetRequestDto dto, @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(budgetService.updateBudget(budgetId, dto, getEmail(principal)));
    }

    @DeleteMapping("/budget/{budgetId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteBudget(@PathVariable UUID budgetId, @AuthenticationPrincipal Object principal) {
        budgetService.deleteBudget(budgetId, getEmail(principal));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/budget/{budgetId}/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CouncilTransactionResponseDto>> getTransactions(@PathVariable UUID budgetId, @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(budgetService.getTransactions(budgetId, getEmail(principal)));
    }

    @PostMapping("/budget/{budgetId}/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilTransactionResponseDto> addTransaction(@PathVariable UUID budgetId, @Valid @RequestBody CouncilTransactionRequestDto dto, @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(budgetService.addTransaction(budgetId, dto, getEmail(principal)));
    }

    @PutMapping("/budget/transactions/{transactionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilTransactionResponseDto> updateTransaction(@PathVariable UUID transactionId, @Valid @RequestBody CouncilTransactionRequestDto dto, @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(budgetService.updateTransaction(transactionId, dto, getEmail(principal)));
    }

    @DeleteMapping("/budget/transactions/{transactionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID transactionId, @AuthenticationPrincipal Object principal) {
        budgetService.deleteTransaction(transactionId, getEmail(principal));
        return ResponseEntity.noContent().build();
    }

    private String getEmail(Object principal) {
        if (principal instanceof UserDetails) return ((UserDetails) principal).getUsername();
        return principal.toString();
    }
}