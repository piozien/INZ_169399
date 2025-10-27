package pl.su.su_backend.service.budget;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.su.su_backend.dto.budget.ClassTransactionRequestDto;
import pl.su.su_backend.dto.budget.ClassTransactionResponseDto;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.budget.ClassBudget;
import pl.su.su_backend.model.budget.ClassTransaction;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.enums.TransactionType;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.budget.ClassBudgetRepository;
import pl.su.su_backend.repositories.budget.ClassTransactionRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.testsupport.Fixtures;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassTransactionServiceTest {

    @Mock
    private ClassTransactionRepository transactionRepository;
    @Mock
    private ClassBudgetRepository budgetRepository;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private ClassBudgetService budgetService;

    @InjectMocks
    private ClassTransactionService classTransactionService;

    private Users testUser;
    private Users testPayerUser;
    private Classes testClass;
    private ClassBudget testBudget;
    private ClassTransaction testTransaction;
    private ClassTransactionRequestDto testRequestDto;
    private String testEmail = "test@test.com";

    @BeforeEach
    void setUp() {
        testUser = Fixtures.userWithStatus("Test User", testEmail, StatusEnum.CONFIRMED);
        testPayerUser = Fixtures.userWithStatus("Payer User", "payer@test.com", StatusEnum.CONFIRMED);
        testClass = Fixtures.schoolClass("1A", "2025");
        testBudget = Fixtures.classBudget(testClass, new BigDecimal("1000"), testUser);
        testTransaction = Fixtures.classTransaction(testBudget, TransactionType.INCOME, new BigDecimal("100"), "Test transaction", testUser);

        testRequestDto = new ClassTransactionRequestDto();
        testRequestDto.setBudgetId(testBudget.getId());
        testRequestDto.setType(TransactionType.INCOME);
        testRequestDto.setAmount(new BigDecimal("100"));
        testRequestDto.setDescription("Test transaction");
        testRequestDto.setDate(LocalDateTime.now());
        testRequestDto.setPayerUserId(testPayerUser.getId());
    }

    @Test
    void createTransaction_ShouldCreateSuccessfully_WhenValidData() {
        // Given
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(usersRepository.findById(testPayerUser.getId())).thenReturn(Optional.of(testPayerUser));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_CREATE))
                .thenReturn(true);
        when(transactionRepository.save(any(ClassTransaction.class))).thenReturn(testTransaction);
        doNothing().when(budgetService).updateBudgetBalance(testBudget.getId());

        // When
        ClassTransactionResponseDto result = classTransactionService.createTransaction(testRequestDto, testUser.getId());

        // Then
        assertNotNull(result);
        verify(budgetRepository).findById(testBudget.getId());
        verify(usersRepository).findById(testUser.getId());
        verify(usersRepository).findById(testPayerUser.getId());
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_CREATE);
        verify(transactionRepository).save(any(ClassTransaction.class));
        verify(budgetService).updateBudgetBalance(testBudget.getId());
        verify(activityLogService).log(eq(testUser.getId()), any(), anyString());
    }

    @Test
    void createTransaction_ShouldCreateSuccessfully_WhenNoPayerUser() {
        // Given
        testRequestDto.setPayerUserId(null);
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_CREATE)).thenReturn(true);
        when(transactionRepository.save(any(ClassTransaction.class))).thenReturn(testTransaction);
        doNothing().when(budgetService).updateBudgetBalance(testBudget.getId());

        // When
        ClassTransactionResponseDto result = classTransactionService.createTransaction(testRequestDto, testUser.getId());

        // Then
        assertNotNull(result);
        verify(usersRepository, never()).findById(testPayerUser.getId());
        verify(transactionRepository).save(any(ClassTransaction.class));
    }

    @Test
    void createTransaction_ShouldThrowException_WhenBudgetNotFound() {
        // Given
        UUID nonExistentBudgetId = UUID.randomUUID();
        testRequestDto.setBudgetId(nonExistentBudgetId);
        when(budgetRepository.findById(nonExistentBudgetId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classTransactionService.createTransaction(testRequestDto, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("Budget not found"));
        verify(budgetRepository).findById(nonExistentBudgetId);
    }

    @Test
    void createTransaction_ShouldThrowException_WhenUserNotFound() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(usersRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classTransactionService.createTransaction(testRequestDto, nonExistentUserId));
        
        assertTrue(exception.getMessage().contains("User not found"));
        verify(usersRepository).findById(nonExistentUserId);
    }

    @Test
    void createTransaction_ShouldThrowException_WhenPayerUserNotFound() {
        // Given
        UUID nonExistentPayerId = UUID.randomUUID();
        testRequestDto.setPayerUserId(nonExistentPayerId);
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_CREATE))
                .thenReturn(true);
        when(usersRepository.findById(nonExistentPayerId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classTransactionService.createTransaction(testRequestDto, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("Payer user not found"));
        verify(usersRepository).findById(nonExistentPayerId);
    }

    @Test
    void createTransaction_ShouldThrowException_WhenNoPermission() {
        // Given
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_TRANSACTION_CREATE)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classTransactionService.createTransaction(testRequestDto, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("not authorized"));
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_TRANSACTION_CREATE);
    }

    @Test
    void getBudgetTransactions_ShouldReturnTransactions_WhenHasPermission() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_VIEW))
                .thenReturn(true);
        when(transactionRepository.findByBudget_IdOrderByDateDesc(testBudget.getId())).thenReturn(List.of(testTransaction));

        // When
        List<ClassTransactionResponseDto> result = classTransactionService.getBudgetTransactions(testBudget.getId(), testEmail);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(usersRepository).findByEmail(testEmail);
        verify(budgetRepository).findById(testBudget.getId());
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_VIEW);
        verify(transactionRepository).findByBudget_IdOrderByDateDesc(testBudget.getId());
    }

    @Test
    void getBudgetTransactions_ShouldThrowException_WhenUserNotFound() {
        // Given
        String nonExistentEmail = "nonexistent@test.com";
        when(usersRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classTransactionService.getBudgetTransactions(testBudget.getId(), nonExistentEmail));
        
        assertTrue(exception.getMessage().contains("User not found"));
        verify(usersRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void getBudgetTransactions_ShouldThrowException_WhenBudgetNotFound() {
        // Given
        UUID nonExistentBudgetId = UUID.randomUUID();
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findById(nonExistentBudgetId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classTransactionService.getBudgetTransactions(nonExistentBudgetId, testEmail));
        
        assertTrue(exception.getMessage().contains("Budget not found"));
        verify(budgetRepository).findById(nonExistentBudgetId);
    }

    @Test
    void getBudgetTransactions_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_TRANSACTION_VIEW)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classTransactionService.getBudgetTransactions(testBudget.getId(), testEmail));
        
        assertTrue(exception.getMessage().contains("Access denied"));
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_VIEW);
    }

    @Test
    void getClassTransactions_ShouldReturnTransactions_WhenHasPermission() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_VIEW))
                .thenReturn(true);
        when(transactionRepository.findByBudget_Classes_IdOrderByDateDesc(testClass.getId())).thenReturn(List.of(testTransaction));

        // When
        List<ClassTransactionResponseDto> result = classTransactionService.getClassTransactions(testClass.getId(), testEmail);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(usersRepository).findByEmail(testEmail);
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_VIEW);
        verify(transactionRepository).findByBudget_Classes_IdOrderByDateDesc(testClass.getId());
    }

    @Test
    void getClassTransactions_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_TRANSACTION_VIEW)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classTransactionService.getClassTransactions(testClass.getId(), testEmail));
        
        assertTrue(exception.getMessage().contains("Access denied"));
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_VIEW);
    }

    @Test
    void getUserTransactions_ShouldReturnTransactions_WhenHasPermission() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_TRANSACTION_VIEW)).thenReturn(true);
        when(transactionRepository.findByPayerUser_IdOrderByDateDesc(testUser.getId())).thenReturn(List.of(testTransaction));

        // When
        List<ClassTransactionResponseDto> result = classTransactionService.getUserTransactions(testUser.getId(), testEmail);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(usersRepository).findByEmail(testEmail);
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_TRANSACTION_VIEW);
        verify(transactionRepository).findByPayerUser_IdOrderByDateDesc(testUser.getId());
    }

    @Test
    void getUserTransactions_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.CLASS_TRANSACTION_VIEW)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classTransactionService.getUserTransactions(testUser.getId(), testEmail));
        
        assertTrue(exception.getMessage().contains("Access denied"));
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.CLASS_TRANSACTION_VIEW);
    }

    @Test
    void getTransactionsByType_ShouldReturnTransactions_WhenValidType() {
        // Given
        TransactionType type = TransactionType.INCOME;
        when(transactionRepository.findByTypeOrderByDateDesc(type)).thenReturn(List.of(testTransaction));

        // When
        List<ClassTransactionResponseDto> result = classTransactionService.getTransactionsByType(type);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository).findByTypeOrderByDateDesc(type);
    }

    
    @Test
    void getTransactionsByDateRange_ShouldReturnTransactions_WhenValidRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        when(transactionRepository.findByDateBetweenOrderByDateDesc(startDate, endDate)).thenReturn(List.of(testTransaction));

        // When
        List<ClassTransactionResponseDto> result = classTransactionService.getTransactionsByDateRange(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository).findByDateBetweenOrderByDateDesc(startDate, endDate);
    }

    @Test
    void updateTransaction_ShouldUpdateSuccessfully_WhenValidData() {
        // Given
        Users otherUser = Fixtures.userWithStatus("Other User", "other@test.com", StatusEnum.CONFIRMED);
        testTransaction.setAddedBy(otherUser);
        when(transactionRepository.findById(testTransaction.getId())).thenReturn(Optional.of(testTransaction));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_EDIT))
                .thenReturn(true);
        when(usersRepository.findById(testPayerUser.getId())).thenReturn(Optional.of(testPayerUser));
        when(transactionRepository.save(any(ClassTransaction.class))).thenReturn(testTransaction);
        doNothing().when(budgetService).updateBudgetBalance(testBudget.getId());

        // When
        ClassTransactionResponseDto result = classTransactionService.updateTransaction(testTransaction.getId(),
                testRequestDto, testUser.getId());

        // Then
        assertNotNull(result);
        verify(transactionRepository).findById(testTransaction.getId());
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_EDIT);
        verify(usersRepository).findById(testPayerUser.getId());
        verify(transactionRepository).save(any(ClassTransaction.class));
        verify(budgetService).updateBudgetBalance(testBudget.getId());
        verify(activityLogService).log(eq(testUser.getId()), any(), anyString());
    }

    @Test
    void updateTransaction_ShouldUpdateSuccessfully_WhenUserIsOwner() {
        // Given
        testTransaction.setAddedBy(testUser); // User is the owner
        when(transactionRepository.findById(testTransaction.getId())).thenReturn(Optional.of(testTransaction));
        when(usersRepository.findById(testPayerUser.getId())).thenReturn(Optional.of(testPayerUser));
        when(transactionRepository.save(any(ClassTransaction.class))).thenReturn(testTransaction);
        doNothing().when(budgetService).updateBudgetBalance(testBudget.getId());

        // When
        ClassTransactionResponseDto result = classTransactionService.updateTransaction(testTransaction.getId(),
                testRequestDto, testUser.getId());

        // Then
        assertNotNull(result);
        verify(permissionService, never()).canAccessClassBudget(any(), any(), any());
        verify(usersRepository).findById(testPayerUser.getId());
        verify(transactionRepository).save(any(ClassTransaction.class));
    }

    @Test
    void updateTransaction_ShouldThrowException_WhenTransactionNotFound() {
        // Given
        UUID nonExistentTransactionId = UUID.randomUUID();
        when(transactionRepository.findById(nonExistentTransactionId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classTransactionService.updateTransaction(nonExistentTransactionId, testRequestDto, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("Transaction not found"));
        verify(transactionRepository).findById(nonExistentTransactionId);
    }

    @Test
    void updateTransaction_ShouldThrowException_WhenNoPermission() {
        // Given
        Users otherUser = Fixtures.userWithStatus("Other User", "other@test.com", StatusEnum.CONFIRMED);
        testTransaction.setAddedBy(otherUser); // Different user
        when(transactionRepository.findById(testTransaction.getId())).thenReturn(Optional.of(testTransaction));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_EDIT))
                .thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classTransactionService.updateTransaction(testTransaction.getId(), testRequestDto, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("not authorized"));
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_EDIT);
    }

    @Test
    void updateTransaction_ShouldThrowException_WhenPayerUserNotFound() {
        // Given
        UUID nonExistentPayerId = UUID.randomUUID();
        testRequestDto.setPayerUserId(nonExistentPayerId);
        Users otherUser = Fixtures.userWithStatus("Other User", "other@test.com", StatusEnum.CONFIRMED);
        testTransaction.setAddedBy(otherUser); // Different user - will check permissions
        when(transactionRepository.findById(testTransaction.getId())).thenReturn(Optional.of(testTransaction));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_EDIT))
                .thenReturn(true);
        when(usersRepository.findById(nonExistentPayerId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classTransactionService.updateTransaction(testTransaction.getId(), testRequestDto, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("Payer user not found"));
        verify(usersRepository).findById(nonExistentPayerId);
    }

    @Test
    void deleteTransaction_ShouldDeleteSuccessfully_WhenValidData() {
        // Given
        Users otherUser = Fixtures.userWithStatus("Other User", "other@test.com", StatusEnum.CONFIRMED);
        testTransaction.setAddedBy(otherUser); // Different user - will check permissions
        when(transactionRepository.findById(testTransaction.getId())).thenReturn(Optional.of(testTransaction));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_DELETE)).thenReturn(true);
        doNothing().when(transactionRepository).delete(any(ClassTransaction.class));
        doNothing().when(budgetService).updateBudgetBalance(testBudget.getId());

        // When
        classTransactionService.deleteTransaction(testTransaction.getId(), testUser.getId());

        // Then
        verify(transactionRepository).findById(testTransaction.getId());
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_DELETE);
        verify(transactionRepository).delete(any(ClassTransaction.class));
        verify(budgetService).updateBudgetBalance(testBudget.getId());
        verify(activityLogService).log(eq(testUser.getId()), any(), anyString());
    }

    @Test
    void deleteTransaction_ShouldDeleteSuccessfully_WhenUserIsOwner() {
        // Given
        testTransaction.setAddedBy(testUser); // User is the owner
        when(transactionRepository.findById(testTransaction.getId())).thenReturn(Optional.of(testTransaction));
        doNothing().when(transactionRepository).delete(any(ClassTransaction.class));
        doNothing().when(budgetService).updateBudgetBalance(testBudget.getId());

        // When
        classTransactionService.deleteTransaction(testTransaction.getId(), testUser.getId());

        // Then
        verify(permissionService, never()).canAccessClassBudget(any(), any(), any());
        verify(transactionRepository).delete(any(ClassTransaction.class));
    }

    @Test
    void deleteTransaction_ShouldThrowException_WhenTransactionNotFound() {
        // Given
        UUID nonExistentTransactionId = UUID.randomUUID();
        when(transactionRepository.findById(nonExistentTransactionId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classTransactionService.deleteTransaction(nonExistentTransactionId, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("Transaction not found"));
        verify(transactionRepository).findById(nonExistentTransactionId);
    }

    @Test
    void deleteTransaction_ShouldThrowException_WhenNoPermission() {
        // Given
        Users otherUser = Fixtures.userWithStatus("Other User", "other@test.com", StatusEnum.CONFIRMED);
        testTransaction.setAddedBy(otherUser); // Different user
        when(transactionRepository.findById(testTransaction.getId())).thenReturn(Optional.of(testTransaction));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_DELETE)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classTransactionService.deleteTransaction(testTransaction.getId(), testUser.getId()));
        
        assertTrue(exception.getMessage().contains("not authorized"));
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_TRANSACTION_DELETE);
    }
}
