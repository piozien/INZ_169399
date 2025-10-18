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
@CrossOrigin(origins = "*")
public class ClassBudgetController {

    private final ClassBudgetService budgetService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClassBudgetResponseDto> createBudget(@Valid @RequestBody ClassBudgetRequestDto dto,
                                                              @AuthenticationPrincipal User principal) {
        log.info("Creating budget for class {} by user {}", dto.getClassId(), principal.getUsername());
        try {
            UUID userId = userService.getCurrentUserId(principal.getUsername());
            ClassBudgetResponseDto budget = budgetService.createBudget(dto, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(budget);
        } catch (Exception e) {
            log.error("Failed to create budget: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-classes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClassBudgetResponseDto>> getMyClassBudget(@AuthenticationPrincipal User principal) {
        log.info("Fetching budgets for user: {}", principal.getUsername());
        try {
            List<ClassBudgetResponseDto> budgets = budgetService.getAllBudgets(principal.getUsername());
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            log.error("Failed to fetch budgets for user {}: {}", principal.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClassBudgetResponseDto>> getClassBudgets(@PathVariable UUID classId,
                                                                        @AuthenticationPrincipal User principal) {
        log.info("Fetching budgets for class: {} by user: {}", classId, principal.getUsername());
        try {
            List<ClassBudgetResponseDto> budgets = budgetService.getClassBudgets(classId, principal.getUsername());
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            log.error("Failed to fetch budgets for class {}: {}", classId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{budgetId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClassBudgetResponseDto> getBudgetById(@PathVariable UUID budgetId,
                                                               @AuthenticationPrincipal User principal) {
        log.info("Fetching budget with ID: {} by user: {}", budgetId, principal.getUsername());
        try {
            ClassBudgetResponseDto budget = budgetService.getBudgetById(budgetId, principal.getUsername());
            return ResponseEntity.ok(budget);
        } catch (Exception e) {
            log.error("Failed to fetch budget {}: {}", budgetId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/class/{classId}/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClassBudgetResponseDto> getCurrentYearBudget(@PathVariable UUID classId,
                                                                       @AuthenticationPrincipal User principal) {
        log.info("Fetching current year budget for class: {} by user: {}", classId, principal.getUsername());
        try {
            ClassBudgetResponseDto budget = budgetService.getCurrentYearBudget(classId, principal.getUsername());
            return ResponseEntity.ok(budget);
        } catch (Exception e) {
            log.error("Failed to fetch current year budget for class {}: {}", classId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{budgetId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClassBudgetResponseDto> updateBudget(@PathVariable UUID budgetId,
                                                              @Valid @RequestBody ClassBudgetRequestDto dto,
                                                              @AuthenticationPrincipal User principal) {
        log.info("Updating budget {} by user {}", budgetId, principal.getUsername());
        try {
            UUID userId = userService.getCurrentUserId(principal.getUsername());
            ClassBudgetResponseDto budget = budgetService.updateBudget(budgetId, dto, userId);
            return ResponseEntity.ok(budget);
        } catch (Exception e) {
            log.error("Failed to update budget {}: {}", budgetId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{budgetId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteBudget(@PathVariable UUID budgetId,
                                           @AuthenticationPrincipal User principal) {
        log.info("Deleting budget {} by user {}", budgetId, principal.getUsername());
        try {
            UUID userId = userService.getCurrentUserId(principal.getUsername());
            budgetService.deleteBudget(budgetId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete budget {}: {}", budgetId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
