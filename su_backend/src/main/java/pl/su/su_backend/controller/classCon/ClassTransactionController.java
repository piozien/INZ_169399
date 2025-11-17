package pl.su.su_backend.controller.classCon;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.budget.ClassTransactionRequestDto;
import pl.su.su_backend.dto.budget.ClassTransactionResponseDto;
import pl.su.su_backend.model.enums.TransactionType;
import pl.su.su_backend.service.auth.AuthenticationService;
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
    private final AuthenticationService authenticationService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CLASS_TRANSACTION_CREATE')")
    public ResponseEntity<ClassTransactionResponseDto> createTransaction(@Valid @RequestBody ClassTransactionRequestDto dto,
                                                                        @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Creating transaction for budget {} by user {}", dto.getBudgetId(), email);
        UUID userId = userService.getCurrentUserId(email);
        ClassTransactionResponseDto transaction = transactionService.createTransaction(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/budget/{budgetId}")
    @PreAuthorize("hasPermission(null, 'CLASS_TRANSACTION_VIEW')")
    public ResponseEntity<List<ClassTransactionResponseDto>> getBudgetTransactions(@PathVariable UUID budgetId,
                                                                                   @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching transactions for budget: {} by user: {}", budgetId, email);
        List<ClassTransactionResponseDto> transactions = transactionService.getBudgetTransactions(budgetId, email);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasPermission(null, 'CLASS_TRANSACTION_VIEW')")
    public ResponseEntity<List<ClassTransactionResponseDto>> getClassTransactions(@PathVariable UUID classId,
                                                                                  @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching transactions for class: {} by user: {}", classId, email);
        List<ClassTransactionResponseDto> transactions = transactionService.getClassTransactions(classId, email);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasPermission(null, 'CLASS_TRANSACTION_VIEW')")
    public ResponseEntity<List<ClassTransactionResponseDto>> getUserTransactions(@PathVariable UUID userId,
                                                                                @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching transactions for user: {} by user: {}", userId, email);
        List<ClassTransactionResponseDto> transactions = transactionService.getUserTransactions(userId, email);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasPermission(null, 'CLASS_TRANSACTION_VIEW')")
    public ResponseEntity<List<ClassTransactionResponseDto>> getTransactionsByType(@PathVariable TransactionType type,
                                                                                   @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching transactions by type: {} by user: {}", type, email);
        List<ClassTransactionResponseDto> transactions = transactionService.getTransactionsByType(type);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasPermission(null, 'CLASS_TRANSACTION_VIEW')")
    public ResponseEntity<List<ClassTransactionResponseDto>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching transactions between {} and {} by user: {}", startDate, endDate, email);
        List<ClassTransactionResponseDto> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }


    @PutMapping("/{transactionId}")
    @PreAuthorize("hasPermission(null, 'CLASS_TRANSACTION_EDIT')")
    public ResponseEntity<ClassTransactionResponseDto> updateTransaction(@PathVariable UUID transactionId,
                                                                        @Valid @RequestBody ClassTransactionRequestDto dto,
                                                                        @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Updating transaction {} by user {}", transactionId, email);
        UUID userId = userService.getCurrentUserId(email);
        ClassTransactionResponseDto transaction = transactionService.updateTransaction(transactionId, dto, userId);
        return ResponseEntity.ok(transaction);
    }


    @DeleteMapping("/{transactionId}")
    @PreAuthorize("hasPermission(null, 'CLASS_TRANSACTION_DELETE')")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID transactionId,
                                                @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Deleting transaction {} by user {}", transactionId, email);
        UUID userId = userService.getCurrentUserId(email);
        transactionService.deleteTransaction(transactionId, userId);
        return ResponseEntity.noContent().build();
    }
}