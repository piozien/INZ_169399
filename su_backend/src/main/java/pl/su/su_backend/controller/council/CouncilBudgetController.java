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
    @PreAuthorize("hasPermission(null, 'COUNCIL_BUDGET_VIEW')")
    public ResponseEntity<CouncilBudgetResponseDto> getBudget(@PathVariable UUID councilId,
                                                              @AuthenticationPrincipal Object principal) {
        String email = getEmail(principal);
        return ResponseEntity.ok(budgetService.getBudget(councilId, email));
    }

    @PostMapping("/{councilId}/budget")
    @PreAuthorize("hasPermission(null, 'COUNCIL_BUDGET_MANAGE')")
    public ResponseEntity<CouncilBudgetResponseDto> createBudget(@PathVariable UUID councilId,
                                                                 @Valid @RequestBody CouncilBudgetRequestDto dto,
                                                                 @AuthenticationPrincipal Object principal) {
        String email = getEmail(principal);
        return ResponseEntity.ok(budgetService.createBudget(councilId, dto, email));
    }

    @GetMapping("/budget/{budgetId}/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CouncilTransactionResponseDto>> getTransactions(@PathVariable UUID budgetId,
                                                                               @AuthenticationPrincipal Object principal) {
        String email = getEmail(principal);
        return ResponseEntity.ok(budgetService.getTransactions(budgetId, email));
    }

    @PostMapping("/budget/{budgetId}/transactions")
    @PreAuthorize("hasPermission(null, 'COUNCIL_BUDGET_MANAGE')")
    public ResponseEntity<CouncilTransactionResponseDto> addTransaction(@PathVariable UUID budgetId,
                                                                        @Valid @RequestBody CouncilTransactionRequestDto dto,
                                                                        @AuthenticationPrincipal Object principal) {
        String email = getEmail(principal);
        return ResponseEntity.ok(budgetService.addTransaction(budgetId, dto, email));
    }

    private String getEmail(Object principal) {
        if (principal instanceof UserDetails userDetails) return userDetails.getUsername();
        return principal.toString();
    }
}