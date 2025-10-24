package pl.su.su_backend.controller.classCon;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.budget.ClassBudgetRequestDto;
import pl.su.su_backend.dto.budget.ClassBudgetResponseDto;
import pl.su.su_backend.service.budget.ClassBudgetService;
import pl.su.su_backend.service.user.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/class-budgets")
@RequiredArgsConstructor
@Slf4j
public class ClassBudgetController {

    private final ClassBudgetService budgetService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CLASS_BUDGET_CREATE')")
    public ResponseEntity<ClassBudgetResponseDto> createBudget(@Valid @RequestBody ClassBudgetRequestDto dto,
                                                              @AuthenticationPrincipal User principal) {
        log.info("Creating budget for class {} by user {}", dto.getClassId(), principal.getUsername());
        UUID userId = userService.getCurrentUserId(principal.getUsername());
        ClassBudgetResponseDto budget = budgetService.createBudget(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(budget);
    }

    @GetMapping("/my-classes")
    @PreAuthorize("hasPermission(null, 'CLASS_BUDGET_VIEW')")
    public ResponseEntity<List<ClassBudgetResponseDto>> getMyClassBudget(@AuthenticationPrincipal User principal) {
        log.info("Fetching budgets for user: {}", principal.getUsername());
        List<ClassBudgetResponseDto> budgets = budgetService.getAllBudgets(principal.getUsername());
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasPermission(null, 'CLASS_BUDGET_VIEW')")
    public ResponseEntity<List<ClassBudgetResponseDto>> getClassBudgets(@PathVariable UUID classId,
                                                                        @AuthenticationPrincipal User principal) {
        log.info("Fetching budgets for class: {} by user: {}", classId, principal.getUsername());
        List<ClassBudgetResponseDto> budgets = budgetService.getClassBudgets(classId, principal.getUsername());
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/{budgetId}")
    @PreAuthorize("hasPermission(null, 'CLASS_BUDGET_VIEW')")
    public ResponseEntity<ClassBudgetResponseDto> getBudgetById(@PathVariable UUID budgetId,
                                                               @AuthenticationPrincipal User principal) {
        log.info("Fetching budget with ID: {} by user: {}", budgetId, principal.getUsername());
        ClassBudgetResponseDto budget = budgetService.getBudgetById(budgetId, principal.getUsername());
        return ResponseEntity.ok(budget);
    }

    @GetMapping("/class/{classId}/current")
    @PreAuthorize("hasPermission(null, 'CLASS_BUDGET_VIEW')")
    public ResponseEntity<ClassBudgetResponseDto> getCurrentYearBudget(@PathVariable UUID classId,
                                                                       @AuthenticationPrincipal User principal) {
        log.info("Fetching current year budget for class: {} by user: {}", classId, principal.getUsername());
        ClassBudgetResponseDto budget = budgetService.getCurrentYearBudget(classId, principal.getUsername());
        return ResponseEntity.ok(budget);
    }

    @PutMapping("/{budgetId}")
    @PreAuthorize("hasPermission(null, 'CLASS_BUDGET_EDIT')")
    public ResponseEntity<ClassBudgetResponseDto> updateBudget(@PathVariable UUID budgetId,
                                                              @Valid @RequestBody ClassBudgetRequestDto dto,
                                                              @AuthenticationPrincipal User principal) {
        log.info("Updating budget {} by user {}", budgetId, principal.getUsername());
        UUID userId = userService.getCurrentUserId(principal.getUsername());
        ClassBudgetResponseDto budget = budgetService.updateBudget(budgetId, dto, userId);
        return ResponseEntity.ok(budget);
    }

    @DeleteMapping("/{budgetId}")
    @PreAuthorize("hasPermission(null, 'CLASS_BUDGET_DELETE')")
    public ResponseEntity<Void> deleteBudget(@PathVariable UUID budgetId,
                                           @AuthenticationPrincipal User principal) {
        log.info("Deleting budget {} by user {}", budgetId, principal.getUsername());
        UUID userId = userService.getCurrentUserId(principal.getUsername());
        budgetService.deleteBudget(budgetId, userId);
        return ResponseEntity.noContent().build();
    }
}
