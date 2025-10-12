package pl.su.su_backend.controller.classCon;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.budget.ClassTransactionRequestDto;
import pl.su.su_backend.dto.budget.ClassTransactionResponseDto;
import pl.su.su_backend.model.enums.TransactionType;
import pl.su.su_backend.service.budget.ClassTransactionService;
import pl.su.su_backend.service.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/class-transactions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClassTransactionController {

    private final ClassTransactionService transactionService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClassTransactionResponseDto> createTransaction(@Valid @RequestBody ClassTransactionRequestDto dto,
                                                                        @AuthenticationPrincipal User principal) {
        log.info("Creating transaction for budget {} by user {}", dto.getBudgetId(), principal.getUsername());
        try {
            UUID userId = userService.getCurrentUserId(principal.getUsername());
            ClassTransactionResponseDto transaction = transactionService.createTransaction(dto, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (Exception e) {
            log.error("Failed to create transaction: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/budget/{budgetId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClassTransactionResponseDto>> getBudgetTransactions(@PathVariable UUID budgetId, 
                                                                                   @AuthenticationPrincipal User principal) {
        log.info("Fetching transactions for budget: {} by user: {}", budgetId, principal.getUsername());
        try {
            List<ClassTransactionResponseDto> transactions = transactionService.getBudgetTransactions(budgetId, principal.getUsername());
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Failed to fetch transactions for budget {}: {}", budgetId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClassTransactionResponseDto>> getClassTransactions(@PathVariable UUID classId, 
                                                                                  @AuthenticationPrincipal User principal) {
        log.info("Fetching transactions for class: {} by user: {}", classId, principal.getUsername());
        try {
            List<ClassTransactionResponseDto> transactions = transactionService.getClassTransactions(classId, principal.getUsername());
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Failed to fetch transactions for class {}: {}", classId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClassTransactionResponseDto>> getUserTransactions(@PathVariable UUID userId, 
                                                                                @AuthenticationPrincipal User principal) {
        log.info("Fetching transactions for user: {} by user: {}", userId, principal.getUsername());
        try {
            List<ClassTransactionResponseDto> transactions = transactionService.getUserTransactions(userId, principal.getUsername());
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Failed to fetch transactions for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClassTransactionResponseDto>> getTransactionsByType(@PathVariable TransactionType type, 
                                                                                   @AuthenticationPrincipal User principal) {
        log.info("Fetching transactions by type: {} by user: {}", type, principal.getUsername());
        try {
            List<ClassTransactionResponseDto> transactions = transactionService.getTransactionsByType(type);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Failed to fetch transactions by type {}: {}", type, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClassTransactionResponseDto>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal User principal) {
        log.info("Fetching transactions between {} and {} by user: {}", startDate, endDate, principal.getUsername());
        try {
            List<ClassTransactionResponseDto> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Failed to fetch transactions by date range: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/unconfirmed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClassTransactionResponseDto>> getUnconfirmedTransactions(@AuthenticationPrincipal User principal) {
        log.info("Fetching unconfirmed transactions by user: {}", principal.getUsername());
        try {
            List<ClassTransactionResponseDto> transactions = transactionService.getUnconfirmedTransactions();
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Failed to fetch unconfirmed transactions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{transactionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClassTransactionResponseDto> updateTransaction(@PathVariable UUID transactionId,
                                                                        @Valid @RequestBody ClassTransactionRequestDto dto,
                                                                        @AuthenticationPrincipal User principal) {
        log.info("Updating transaction {} by user {}", transactionId, principal.getUsername());
        try {
            UUID userId = userService.getCurrentUserId(principal.getUsername());
            ClassTransactionResponseDto transaction = transactionService.updateTransaction(transactionId, dto, userId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Failed to update transaction {}: {}", transactionId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{transactionId}/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClassTransactionResponseDto> confirmTransaction(@PathVariable UUID transactionId,
                                                                         @AuthenticationPrincipal User principal) {
        log.info("Confirming transaction {} by user {}", transactionId, principal.getUsername());
        try {
            UUID userId = userService.getCurrentUserId(principal.getUsername());
            ClassTransactionResponseDto transaction = transactionService.confirmTransaction(transactionId, userId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Failed to confirm transaction {}: {}", transactionId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{transactionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID transactionId,
                                                @AuthenticationPrincipal User principal) {
        log.info("Deleting transaction {} by user {}", transactionId, principal.getUsername());
        try {
            UUID userId = userService.getCurrentUserId(principal.getUsername());
            transactionService.deleteTransaction(transactionId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete transaction {}: {}", transactionId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}