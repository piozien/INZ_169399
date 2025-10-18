package pl.su.su_backend.service.budget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.budget.ClassTransactionRequestDto;
import pl.su.su_backend.dto.budget.ClassTransactionResponseDto;
import pl.su.su_backend.dto.budget.ClassTransactionMapper;
import pl.su.su_backend.model.budget.ClassBudget;
import pl.su.su_backend.model.budget.ClassTransaction;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.TransactionType;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.budget.ClassBudgetRepository;
import pl.su.su_backend.repositories.budget.ClassTransactionRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassTransactionService {

    private final ClassTransactionRepository transactionRepository;
    private final ClassBudgetRepository budgetRepository;
    private final UsersRepository usersRepository;
    private final ActivityLogService activityLogService;
    private final PermissionService permissionService;
    private final ClassBudgetService budgetService;

    public ClassTransactionResponseDto createTransaction(ClassTransactionRequestDto dto, UUID addedById) {
        log.info("Creating transaction for budget {} by user {}", dto.getBudgetId(), addedById);
        
        ClassBudget budget = budgetRepository.findById(dto.getBudgetId())
                .orElseThrow(() -> new RuntimeException("Budget not found: " + dto.getBudgetId()));
        
        Users addedBy = usersRepository.findById(addedById)
                .orElseThrow(() -> new RuntimeException("User not found: " + addedById));

        if (!permissionService.canAccessClassBudget(addedById, budget.getClasses().getId(), PermissionCode.CLASS_TRANSACTION_CREATE)) {
            throw new RuntimeException("You are not authorized to create transactions for this budget");
        }

        Users payerUser = null;
        if (dto.getPayerUserId() != null) {
            payerUser = usersRepository.findById(dto.getPayerUserId())
                    .orElseThrow(() -> new RuntimeException("Payer user not found: " + dto.getPayerUserId()));
        }

        ClassTransaction transaction = ClassTransaction.builder()
                .budget(budget)
                .type(dto.getType())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .date(dto.getDate())
                .addedBy(addedBy)
                .payerUser(payerUser)
                .build();

        ClassTransaction savedTransaction = transactionRepository.save(transaction);
        
        budgetService.updateBudgetBalance(dto.getBudgetId());
        
        activityLogService.log(addedById, ActionType.TRANSACTION_CREATE, 
                "Created transaction: " + dto.getDescription() + " (" + dto.getAmount() + " zł)");
        
        log.info("Transaction created successfully with ID: {}", savedTransaction.getId());
        return ClassTransactionMapper.toResponse(savedTransaction);
    }

    @Transactional(readOnly = true)
    public List<ClassTransactionResponseDto> getBudgetTransactions(UUID budgetId, String currentUserEmail) {
        log.info("Fetching transactions for budget: {} by user: {}", budgetId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        ClassBudget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + budgetId));
        
        if (!permissionService.canAccessClassBudget(user.getId(), budget.getClasses().getId(), PermissionCode.CLASS_TRANSACTION_VIEW)) {
            throw new RuntimeException("Access denied: User must have class transaction viewing permission");
        }
        
        List<ClassTransaction> transactions = transactionRepository.findByBudget_IdOrderByDateDesc(budgetId);
        List<ClassTransactionResponseDto> result = new ArrayList<>();
        for (ClassTransaction transaction : transactions) {
            result.add(ClassTransactionMapper.toResponse(transaction));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ClassTransactionResponseDto> getClassTransactions(UUID classId, String currentUserEmail) {
        log.info("Fetching transactions for class: {} by user: {}", classId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.canAccessClassBudget(user.getId(), classId, PermissionCode.CLASS_TRANSACTION_VIEW)) {
            throw new RuntimeException("Access denied: User must have class transaction viewing permission");
        }
        
        List<ClassTransaction> transactions = transactionRepository.findByBudget_Classes_IdOrderByDateDesc(classId);
        List<ClassTransactionResponseDto> result = new ArrayList<>();
        for (ClassTransaction transaction : transactions) {
            result.add(ClassTransactionMapper.toResponse(transaction));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ClassTransactionResponseDto> getUserTransactions(UUID userId, String currentUserEmail) {
        log.info("Fetching transactions for user: {} by user: {}", userId, currentUserEmail);
        
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.CLASS_TRANSACTION_VIEW)) {
            throw new RuntimeException("Access denied: User must have class transaction viewing permission");
        }
        
        List<ClassTransaction> transactions = transactionRepository.findByPayerUser_IdOrderByDateDesc(userId);
        List<ClassTransactionResponseDto> result = new ArrayList<>();
        for (ClassTransaction transaction : transactions) {
            result.add(ClassTransactionMapper.toResponse(transaction));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ClassTransactionResponseDto> getTransactionsByType(TransactionType type) {
        log.info("Fetching transactions by type: {}", type);
        List<ClassTransaction> transactions = transactionRepository.findByTypeOrderByDateDesc(type);
        List<ClassTransactionResponseDto> result = new ArrayList<>();
        for (ClassTransaction transaction : transactions) {
            result.add(ClassTransactionMapper.toResponse(transaction));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ClassTransactionResponseDto> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching transactions between {} and {}", startDate, endDate);
        return transactionRepository.findByDateBetweenOrderByDateDesc(startDate, endDate).stream()
                .map(ClassTransactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ClassTransactionResponseDto updateTransaction(UUID transactionId, ClassTransactionRequestDto dto, UUID updatedById) {
        log.info("Updating transaction {} by user {}", transactionId, updatedById);
        
        ClassTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        if (!transaction.getAddedBy().getId().equals(updatedById) && 
            !permissionService.canAccessClassBudget(updatedById, transaction.getBudget().getClasses().getId(),
                    PermissionCode.CLASS_TRANSACTION_EDIT)) {
            throw new RuntimeException("You are not authorized to edit this transaction");
        }

        transaction.setType(dto.getType());
        transaction.setAmount(dto.getAmount());
        transaction.setDescription(dto.getDescription());
        transaction.setDate(dto.getDate());

        if (dto.getPayerUserId() != null) {
            Users payerUser = usersRepository.findById(dto.getPayerUserId())
                    .orElseThrow(() -> new RuntimeException("Payer user not found: " + dto.getPayerUserId()));
            transaction.setPayerUser(payerUser);
        }

        ClassTransaction updatedTransaction = transactionRepository.save(transaction);
        
        budgetService.updateBudgetBalance(transaction.getBudget().getId());
        
        activityLogService.log(updatedById, ActionType.TRANSACTION_EDIT, 
                "Updated transaction: " + dto.getDescription());
        
        log.info("Transaction updated successfully");
        return ClassTransactionMapper.toResponse(updatedTransaction);
    }


    public void deleteTransaction(UUID transactionId, UUID deletedById) {
        log.info("Deleting transaction {} by user {}", transactionId, deletedById);
        
        ClassTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        if (!transaction.getAddedBy().getId().equals(deletedById) && 
            !permissionService.canAccessClassBudget(deletedById, transaction.getBudget().getClasses().getId(),
                    PermissionCode.CLASS_TRANSACTION_DELETE)) {
            throw new RuntimeException("You are not authorized to delete this transaction");
        }

        ClassBudget budget = transaction.getBudget();
        UUID budgetId = budget.getId();
        transactionRepository.delete(transaction);
        
        budgetService.updateBudgetBalance(budgetId);
        
        activityLogService.log(deletedById, ActionType.TRANSACTION_DELETE, 
                "Deleted transaction: " + transaction.getDescription());
        
        log.info("Transaction deleted successfully");
    }


}
