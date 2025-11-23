package pl.su.su_backend.service.budget;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.su.su_backend.dto.budget.ClassBudgetRequestDto;
import pl.su.su_backend.dto.budget.ClassBudgetResponseDto;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.budget.ClassBudget;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.budget.ClassBudgetRepository;
import pl.su.su_backend.repositories.classes.ClassesRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.testsupport.Fixtures;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassBudgetServiceTest {

    @Mock
    private ClassBudgetRepository budgetRepository;
    @Mock
    private ClassesRepository classesRepository;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private ClassBudgetService classBudgetService;

    private Users testUser;
    private Classes testClass;
    private ClassBudget testBudget;
    private ClassBudgetRequestDto testRequestDto;
    private String testEmail = "test@test.com";

    @BeforeEach
    void setUp() {
        testUser = Fixtures.userWithStatus("Test User", testEmail, StatusEnum.CONFIRMED);
        testClass = Fixtures.schoolClass("1A", "2025");
        testBudget = Fixtures.classBudget(testClass, new BigDecimal("1000"), testUser);

        testRequestDto = new ClassBudgetRequestDto();
        testRequestDto.setClassId(testClass.getId());
        testRequestDto.setYear("2025");
        testRequestDto.setInitialAmount(new BigDecimal("1000"));
    }

    
    @Test
    void createBudget_ShouldCreateSuccessfully_WhenValidData() {
        // Given
        when(classesRepository.findById(testClass.getId())).thenReturn(Optional.of(testClass));
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_BUDGET_CREATE)).thenReturn(true);
        when(budgetRepository.findByClasses_IdAndYear(testClass.getId(), "2025")).thenReturn(Optional.empty());
        when(budgetRepository.save(any(ClassBudget.class))).thenReturn(testBudget);

        // When
        ClassBudgetResponseDto result = classBudgetService.createBudget(testClass.getId(), testRequestDto, testUser.getId());

        // Then
        assertNotNull(result);
        verify(classesRepository).findById(testClass.getId());
        verify(usersRepository).findById(testUser.getId());
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_BUDGET_CREATE);
        verify(budgetRepository).save(any(ClassBudget.class));
        verify(activityLogService).log(eq(testUser.getId()), any(), anyString());
    }

    @Test
    void createBudget_ShouldThrowException_WhenClassNotFound() {
        // Given
        UUID nonExistentClassId = UUID.randomUUID();
        when(classesRepository.findById(nonExistentClassId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classBudgetService.createBudget(nonExistentClassId, testRequestDto, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("Class not found"));
        verify(classesRepository).findById(nonExistentClassId);
    }

    @Test
    void createBudget_ShouldThrowException_WhenUserNotFound() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        when(classesRepository.findById(testClass.getId())).thenReturn(Optional.of(testClass));
        when(usersRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classBudgetService.createBudget(testClass.getId(), testRequestDto, nonExistentUserId));
        
        assertTrue(exception.getMessage().contains("User not found"));
        verify(usersRepository).findById(nonExistentUserId);
    }

    @Test
    void createBudget_ShouldThrowException_WhenNoPermission() {
        // Given
        when(classesRepository.findById(testClass.getId())).thenReturn(Optional.of(testClass));
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_BUDGET_CREATE)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classBudgetService.createBudget(testClass.getId(), testRequestDto, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("not authorized"));
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_BUDGET_CREATE);
    }

    @Test
    void createBudget_ShouldThrowException_WhenBudgetAlreadyExists() {
        // Given
        when(classesRepository.findById(testClass.getId())).thenReturn(Optional.of(testClass));
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_BUDGET_CREATE)).thenReturn(true);
        when(budgetRepository.findByClasses_IdAndYear(testClass.getId(), "2025")).thenReturn(Optional.of(testBudget));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classBudgetService.createBudget(testClass.getId(), testRequestDto, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(budgetRepository).findByClasses_IdAndYear(testClass.getId(), "2025");
    }

    
    @Test
    void getBudget_ShouldReturnBudget_WhenHasPermission() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_BUDGET_VIEW)).thenReturn(true);
        when(budgetRepository.findByClasses_IdOrderByYearDesc(testClass.getId())).thenReturn(List.of(testBudget));

        // When
        ClassBudgetResponseDto result = classBudgetService.getBudget(testClass.getId(), testEmail);

        // Then
        assertNotNull(result);
        verify(usersRepository).findByEmail(testEmail);
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_BUDGET_VIEW);
        verify(budgetRepository).findByClasses_IdOrderByYearDesc(testClass.getId());
    }

    @Test
    void getBudget_ShouldThrowException_WhenUserNotFound() {
        // Given
        String nonExistentEmail = "nonexistent@test.com";
        when(usersRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classBudgetService.getBudget(testClass.getId(), nonExistentEmail));
        
        assertTrue(exception.getMessage().contains("User not found"));
        verify(usersRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void getBudget_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_BUDGET_VIEW)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classBudgetService.getBudget(testClass.getId(), testEmail));
        
        assertTrue(exception.getMessage().contains("You are not authorized to view budgets for this class"));
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_BUDGET_VIEW);
    }

    @Test
    void getBudgetById_ShouldReturnBudget_WhenExistsAndHasPermission() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_BUDGET_VIEW)).thenReturn(true);

        // When
        ClassBudgetResponseDto result = classBudgetService.getBudgetById(testBudget.getId(), testEmail);

        // Then
        assertNotNull(result);
        verify(usersRepository).findByEmail(testEmail);
        verify(budgetRepository).findById(testBudget.getId());
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_BUDGET_VIEW);
    }

    @Test
    void getBudgetById_ShouldThrowException_WhenBudgetNotFound() {
        // Given
        UUID nonExistentBudgetId = UUID.randomUUID();
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findById(nonExistentBudgetId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classBudgetService.getBudgetById(nonExistentBudgetId, testEmail));
        
        assertTrue(exception.getMessage().contains("Budget not found"));
        verify(budgetRepository).findById(nonExistentBudgetId);
    }

    @Test
    void getBudgetById_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_BUDGET_VIEW)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classBudgetService.getBudgetById(testBudget.getId(), testEmail));
        
        assertTrue(exception.getMessage().contains("Access denied"));
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_BUDGET_VIEW);
    }

    
    @Test
    void updateBudget_ShouldUpdateSuccessfully_WhenValidData() {
        // Given
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_BUDGET_EDIT)).thenReturn(true);
        when(budgetRepository.save(any(ClassBudget.class))).thenReturn(testBudget);

        // When
        ClassBudgetResponseDto result = classBudgetService.updateBudget(testBudget.getId(), testRequestDto, testUser.getId());

        // Then
        assertNotNull(result);
        verify(budgetRepository).findById(testBudget.getId());
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_BUDGET_EDIT);
        verify(budgetRepository).save(any(ClassBudget.class));
        verify(activityLogService).log(eq(testUser.getId()), any(), anyString());
    }

    @Test
    void updateBudget_ShouldThrowException_WhenBudgetNotFound() {
        // Given
        UUID nonExistentBudgetId = UUID.randomUUID();
        when(budgetRepository.findById(nonExistentBudgetId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classBudgetService.updateBudget(nonExistentBudgetId, testRequestDto, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("Budget not found"));
        verify(budgetRepository).findById(nonExistentBudgetId);
    }

    @Test
    void updateBudget_ShouldThrowException_WhenNoPermission() {
        // Given
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_BUDGET_EDIT)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classBudgetService.updateBudget(testBudget.getId(), testRequestDto, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("not authorized"));
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_BUDGET_EDIT);
    }

    
    @Test
    void deleteBudget_ShouldDeleteSuccessfully_WhenValidData() {
        // Given
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_BUDGET_DELETE)).thenReturn(true);
        doNothing().when(budgetRepository).delete(any(ClassBudget.class));

        // When
        classBudgetService.deleteBudget(testBudget.getId(), testUser.getId());

        // Then
        verify(budgetRepository).findById(testBudget.getId());
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_BUDGET_DELETE);
        verify(budgetRepository).delete(any(ClassBudget.class));
        verify(activityLogService).log(eq(testUser.getId()), any(), anyString());
    }

    @Test
    void deleteBudget_ShouldThrowException_WhenBudgetNotFound() {
        // Given
        UUID nonExistentBudgetId = UUID.randomUUID();
        when(budgetRepository.findById(nonExistentBudgetId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classBudgetService.deleteBudget(nonExistentBudgetId, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("Budget not found"));
        verify(budgetRepository).findById(nonExistentBudgetId);
    }

    @Test
    void deleteBudget_ShouldThrowException_WhenNoPermission() {
        // Given
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(permissionService.canAccessClassBudget(testUser.getId(), testClass.getId(),
                PermissionCode.CLASS_BUDGET_DELETE)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classBudgetService.deleteBudget(testBudget.getId(), testUser.getId()));
        
        assertTrue(exception.getMessage().contains("not authorized"));
        verify(permissionService).canAccessClassBudget(testUser.getId(), testClass.getId(), PermissionCode.CLASS_BUDGET_DELETE);
    }

    
    @Test
    void updateBudgetBalance_ShouldUpdateBalance_WhenValidBudget() {
        // Given
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(any(ClassBudget.class))).thenReturn(testBudget);

        // When
        classBudgetService.updateBudgetBalance(testBudget.getId());

        // Then
        verify(budgetRepository).findById(testBudget.getId());
        verify(budgetRepository).save(any(ClassBudget.class));
    }

    @Test
    void updateBudgetBalance_ShouldThrowException_WhenBudgetNotFound() {
        // Given
        UUID nonExistentBudgetId = UUID.randomUUID();
        when(budgetRepository.findById(nonExistentBudgetId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> classBudgetService.updateBudgetBalance(nonExistentBudgetId));
        
        assertTrue(exception.getMessage().contains("Budget not found"));
        verify(budgetRepository).findById(nonExistentBudgetId);
    }
}
