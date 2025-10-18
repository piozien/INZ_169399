package pl.su.su_backend.service.budget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.budget.ClassBudgetRequestDto;
import pl.su.su_backend.dto.budget.ClassBudgetResponseDto;
import pl.su.su_backend.dto.budget.ClassBudgetMapper;
import pl.su.su_backend.model.budget.ClassBudget;
import pl.su.su_backend.model.budget.ClassTransaction;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.TransactionType;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.budget.ClassBudgetRepository;
import pl.su.su_backend.repositories.classRep.ClassesRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassBudgetService {

    private final ClassBudgetRepository budgetRepository;
    private final ClassesRepository classesRepository;
    private final UsersRepository usersRepository;
    private final ActivityLogService activityLogService;
    private final PermissionService permissionService;

    public ClassBudgetResponseDto createBudget(ClassBudgetRequestDto dto, UUID createdById) {
        log.info("Creating budget for class {} by user {}", dto.getClassId(), createdById);
        
        Classes classes = classesRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found: " + dto.getClassId()));
        
        Users createdBy = usersRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found: " + createdById));

        if (!permissionService.canAccessClassBudget(createdById, dto.getClassId(), PermissionCode.CLASS_BUDGET_CREATE)) {
            throw new RuntimeException("You are not authorized to create budgets for this class");
        }

        String year = dto.getYear() != null ? dto.getYear() : String.valueOf(LocalDateTime.now().getYear());
        if (budgetRepository.findByClasses_IdAndYear(dto.getClassId(), year).isPresent()) {
            throw new RuntimeException("Budget for class " + classes.getName() + " and year " + year + " already exists");
        }

        ClassBudget budget = ClassBudget.builder()
                .classes(classes)
                .year(year)
                .initialAmount(dto.getInitialAmount() != null ? dto.getInitialAmount() : BigDecimal.ZERO)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        ClassBudget savedBudget = budgetRepository.save(budget);
        
        activityLogService.log(createdById, ActionType.BUDGET_CREATE, 
                "Created budget for class: " + classes.getName());
        
        log.info("Budget created successfully with ID: {}", savedBudget.getId());
        return toResponseDto(savedBudget);
    }

    @Transactional(readOnly = true)
    public List<ClassBudgetResponseDto> getClassBudgets(UUID classId, String currentUserEmail) {
        log.info("Fetching budgets for class: {} by user: {}", classId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.canAccessClassBudget(user.getId(), classId, PermissionCode.CLASS_BUDGET_VIEW)) {
            throw new RuntimeException("Access denied: User must have class budget viewing permission");
        }
        
        List<ClassBudget> budgets = budgetRepository.findByClasses_IdOrderByYearDesc(classId);
        List<ClassBudgetResponseDto> result = new ArrayList<>();
        for (ClassBudget budget : budgets) {
            result.add(toResponseDto(budget));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public ClassBudgetResponseDto getBudgetById(UUID budgetId, String currentUserEmail) {
        log.info("Fetching budget with ID: {} by user: {}", budgetId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        ClassBudget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + budgetId));
        
        if (!permissionService.canAccessClassBudget(user.getId(), budget.getClasses().getId(), PermissionCode.CLASS_BUDGET_VIEW)) {
            throw new RuntimeException("Access denied: User must have class budget viewing permission");
        }
        
        return toResponseDto(budget);
    }

    @Transactional(readOnly = true)
    public ClassBudgetResponseDto getCurrentYearBudget(UUID classId, String currentUserEmail) {
        log.info("Fetching current year budget for class: {} by user: {}", classId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.canAccessClassBudget(user.getId(), classId, PermissionCode.CLASS_BUDGET_VIEW)) {
            throw new RuntimeException("You are not authorized to view budgets for this class");
        }
        
        String currentYear = String.valueOf(LocalDateTime.now().getYear());
        ClassBudget budget = budgetRepository.findByClasses_IdAndYear(classId, currentYear)
                .orElseThrow(() -> new RuntimeException("Budget for class " + classId + " and year " + currentYear + " not found"));
        return toResponseDto(budget);
    }

    public ClassBudgetResponseDto updateBudget(UUID budgetId, ClassBudgetRequestDto dto, UUID updatedById) {
        log.info("Updating budget {} by user {}", budgetId, updatedById);
        
        ClassBudget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + budgetId));

        if (!permissionService.canAccessClassBudget(updatedById, budget.getClasses().getId(), PermissionCode.CLASS_BUDGET_EDIT)) {
            throw new RuntimeException("You are not authorized to edit budgets for this class");
        }

        if (dto.getYear() != null) {
            budget.setYear(dto.getYear());
        }
        if (dto.getInitialAmount() != null) {
            budget.setInitialAmount(dto.getInitialAmount());
        }

        ClassBudget updatedBudget = budgetRepository.save(budget);
        
        activityLogService.log(updatedById, ActionType.BUDGET_EDIT, 
                "Updated budget for class: " + budget.getClasses().getName());
        
        log.info("Budget updated successfully");
        return toResponseDto(updatedBudget);
    }

    public void deleteBudget(UUID budgetId, UUID deletedById) {
        log.info("Deleting budget {} by user {}", budgetId, deletedById);
        
        ClassBudget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + budgetId));

        if (!permissionService.canAccessClassBudget(deletedById, budget.getClasses().getId(), PermissionCode.CLASS_BUDGET_DELETE)) {
            throw new RuntimeException("You are not authorized to delete budgets for this class");
        }

        budgetRepository.delete(budget);
        
        activityLogService.log(deletedById, ActionType.BUDGET_DELETE, 
                "Deleted budget for class: " + budget.getClasses().getName());
        
        log.info("Budget deleted successfully");
    }

    private ClassBudgetResponseDto toResponseDto(ClassBudget budget) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (ClassTransaction transaction : budget.getTransactions()) {
            if (transaction.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                totalExpenses = totalExpenses.add(transaction.getAmount());
            }
        }

        ClassBudgetResponseDto dto = ClassBudgetMapper.toResponse(budget);
        
        dto.setTotalIncome(totalIncome);
        dto.setTotalExpenses(totalExpenses);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<ClassBudgetResponseDto> getAllBudgets(String currentUserEmail) {
        log.info("Fetching all budgets by user: {}", currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.CLASS_BUDGET_VIEW)) {
            throw new RuntimeException("You are not authorized to view all budgets");
        }
        
        List<ClassBudget> budgets = budgetRepository.findAll();
        List<ClassBudgetResponseDto> result = new ArrayList<>();
        for (ClassBudget budget : budgets) {
            result.add(toResponseDto(budget));
        }
        return result;
    }

    @Transactional
    public void updateBudgetBalance(UUID budgetId) {
        log.info("Updating balance for budget: {}", budgetId);
        
        ClassBudget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found: " + budgetId));
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (ClassTransaction transaction : budget.getTransactions()) {
            if (transaction.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                totalExpenses = totalExpenses.add(transaction.getAmount());
            }
        }

        BigDecimal newBalance = (budget.getInitialAmount() != null ? budget.getInitialAmount() : BigDecimal.ZERO)
                .add(totalIncome)
                .subtract(totalExpenses);
        
        budget.setBalance(newBalance);
        budgetRepository.save(budget);
        
        log.info("Budget balance updated to: {}", newBalance);
    }

}
