package pl.su.su_backend.service.budget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.budget.*;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.budget.CouncilBudget;
import pl.su.su_backend.model.budget.CouncilTransaction;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.TransactionType;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.budget.CouncilBudgetRepository;
import pl.su.su_backend.repositories.budget.CouncilTransactionRepository;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.service.council.CouncilMemberService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.service.user.UserService;
import pl.su.su_backend.service.auth.PermissionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CouncilBudgetService {

    private final CouncilBudgetRepository councilBudgetRepository;
    private final CouncilTransactionRepository councilTransactionRepository;
    private final CouncilRepository councilRepository;
    private final UserService userService;
    private final CouncilMemberService councilMemberService;
    private final ActivityLogService activityLogService;
    private final CouncilBudgetMapper budgetMapper;
    private final CouncilTransactionMapper transactionMapper;
    private final PermissionService permissionService;


    public CouncilBudgetResponseDto createBudget(UUID councilId, CouncilBudgetRequestDto dto, String currentUserEmail) {
        Users user = userService.getUserByEmailEntity(currentUserEmail);
        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_BUDGET_CREATE, councilId)) {
            throw ApiException.forbidden("Brak uprawnień do tworzenia budżetu");
        }
        Council council = councilRepository.findById(councilId).orElseThrow(() -> ApiException.notFound("Nie znaleziono samorządu"));
        String year = dto.getYear() != null ? dto.getYear() : String.valueOf(LocalDateTime.now().getYear());

        boolean isAdmin = user.getUserRoles().stream().anyMatch(u -> RoleCode.ADMINISTRATOR.equals(u.getRole().
                getRoleCode()));

        if (councilBudgetRepository.findByCouncil_IdAndYear(councilId, year).isPresent()) {
            throw ApiException.conflict("Budżet na rok " + year + " już istnieje");
        }
        if(!isAdmin && !council.isActive()){
            throw ApiException.conflict("Samorząd nie jest aktywny. Nie można stworzyć budżetu!");
        }
        CouncilBudget budget = CouncilBudget.builder().council(council).year(year).initialAmount(dto.getInitialAmount()).balance(dto.getInitialAmount()).createdBy(user).createdAt(LocalDateTime.now()).build();
        CouncilBudget saved = councilBudgetRepository.save(budget);
        activityLogService.log(user.getId(), ActionType.BUDGET_CREATE, "Utworzono budżet " + year);
        return budgetMapper.toResponse(saved);
    }

    public CouncilBudgetResponseDto updateBudget(UUID budgetId, CouncilBudgetRequestDto dto, String currentUserEmail) {
        Users user = userService.getUserByEmailEntity(currentUserEmail);
        CouncilBudget budget = councilBudgetRepository.findById(budgetId).orElseThrow(() -> ApiException.notFound("Budżet nie istnieje"));

        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_BUDGET_EDIT, budget.getCouncil().getId())) {
            throw ApiException.forbidden("Brak uprawnień do edycji ustawień budżetu");
        }

        budget.setYear(dto.getYear());
        if (dto.getInitialAmount() != null && !dto.getInitialAmount().equals(budget.getInitialAmount())) {
            budget.setInitialAmount(dto.getInitialAmount());
            updateBalance(budget);
        } else {
            councilBudgetRepository.save(budget);
        }
        activityLogService.log(user.getId(), ActionType.BUDGET_UPDATE, "Zaktualizowano budżet: " + budget.getYear());
        return budgetMapper.toResponse(budget);
    }

    public void deleteBudget(UUID budgetId, String currentUserEmail) {
        Users user = userService.getUserByEmailEntity(currentUserEmail);
        CouncilBudget budget = councilBudgetRepository.findById(budgetId).orElseThrow(() -> ApiException.notFound("Budżet nie istnieje"));

        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_BUDGET_DELETE, budget.getCouncil().getId())) {
            throw ApiException.forbidden("Brak uprawnień do usunięcia budżetu.");
        }
        councilBudgetRepository.delete(budget);
        activityLogService.log(user.getId(), ActionType.BUDGET_DELETE, "Usunięto budżet: " + budget.getYear());
    }

    @Transactional(readOnly = true)
    public CouncilBudgetResponseDto getBudget(UUID councilId, String currentUserEmail) {
        Users user = userService.getUserByEmailEntity(currentUserEmail);

        CouncilBudget budget = councilBudgetRepository.findByCouncil_IdOrderByYearDesc(councilId).stream().findFirst().orElseThrow(() -> ApiException.notFound("Brak budżetu"));

        boolean hasPerm = permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_BUDGET_VIEW, councilId);
        boolean isMember = councilMemberService.isMemberOfCouncil(user.getId(), councilId);
        if (!hasPerm && !isMember) throw ApiException.forbidden("Brak dostępu do budżetu");

        return budgetMapper.toResponse(budget);
    }

    public CouncilTransactionResponseDto addTransaction(UUID budgetId, CouncilTransactionRequestDto dto, String currentUserEmail) {
        Users user = userService.getUserByEmailEntity(currentUserEmail);
        CouncilBudget budget = councilBudgetRepository.findById(budgetId).orElseThrow(() -> ApiException.notFound("Budżet nie istnieje"));
        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_TRANSACTION_CREATE, budget.getCouncil().getId())) {
            throw ApiException.forbidden("Brak uprawnień");
        }
        if(!budget.getCouncil().isActive()){
            throw ApiException.conflict("Samorząd nie jest aktywny. Nie można dodać transakcji!");
        }

        CouncilTransaction transaction = transactionMapper.toEntity(dto, budget, user);

        if (transaction.getAmount() != null) {
            transaction.setAmount(transaction.getAmount().abs());
        }

        councilTransactionRepository.save(transaction);
        updateBalance(budget);
        activityLogService.log(user.getId(), ActionType.TRANSACTION_CREATE, "Dodano transakcję");
        return transactionMapper.toResponse(transaction);
    }

    public CouncilTransactionResponseDto updateTransaction(UUID transactionId, CouncilTransactionRequestDto dto, String currentUserEmail) {
        Users user = userService.getUserByEmailEntity(currentUserEmail);
        CouncilTransaction transaction = councilTransactionRepository.findById(transactionId).orElseThrow(() -> ApiException.notFound("Transakcja nie istnieje"));

        if (transaction.getAmount() != null) {
            transaction.setAmount(transaction.getAmount().abs());
        }

        CouncilBudget budget = transaction.getBudget();
        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_TRANSACTION_EDIT, budget.getCouncil().getId())) {
            throw ApiException.forbidden("Brak uprawnień");
        }
        if(!budget.getCouncil().isActive()){
            throw ApiException.conflict("Samorząd nie jest aktywny. Nie można edytować transakcji!");
        }
        transaction.setDescription(dto.getDescription());
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setDate(dto.getDate());
        councilTransactionRepository.save(transaction);
        updateBalance(budget);
        activityLogService.log(user.getId(), ActionType.TRANSACTION_EDIT, "Edytowano transakcję " + transaction.getDescription());
        return transactionMapper.toResponse(transaction);
    }

    public void deleteTransaction(UUID transactionId, String currentUserEmail) {
        Users user = userService.getUserByEmailEntity(currentUserEmail);
        CouncilTransaction transaction = councilTransactionRepository.findById(transactionId).orElseThrow(() -> ApiException.notFound("Transakcja nie istnieje"));
        CouncilBudget budget = transaction.getBudget();
        boolean isAdmin = user.getUserRoles().stream().anyMatch(u -> RoleCode.ADMINISTRATOR.equals(u.getRole().
                getRoleCode()));

        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_TRANSACTION_DELETE, budget.getCouncil().getId())) {
            throw ApiException.forbidden("Brak uprawnień");
        }
        if(!isAdmin && !budget.getCouncil().isActive()){
            throw ApiException.conflict("Samorząd nie jest aktywny. Nie można usunać transakcji!");
        }
        councilTransactionRepository.delete(transaction);
        updateBalance(budget);
        activityLogService.log(user.getId(), ActionType.TRANSACTION_DELETE, "Usunięto transakcję");
    }

    private void updateBalance(CouncilBudget budget) {
        BigDecimal income = councilTransactionRepository.findByBudgetId(budget.getId()).stream().filter(t -> t.getType() == TransactionType.INCOME).map(CouncilTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expense = councilTransactionRepository.findByBudgetId(budget.getId()).stream().filter(t -> t.getType() == TransactionType.EXPENSE).map(CouncilTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        budget.setBalance(budget.getInitialAmount().add(income).subtract(expense));
        councilBudgetRepository.save(budget);
    }

    @Transactional(readOnly = true)
    public List<CouncilTransactionResponseDto> getTransactions(UUID budgetId, String currentUserEmail) {
        Users user = userService.getUserByEmailEntity(currentUserEmail);

        CouncilBudget budget = councilBudgetRepository.findById(budgetId)
                .orElseThrow(() -> ApiException.notFound("Budżet nie istnieje"));

        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_TRANSACTION_VIEW, budget.getCouncil().getId())) {
            throw ApiException.forbidden("Brak uprawnień");
        }
        return councilTransactionRepository.findByBudgetId(budgetId).stream().map(transactionMapper::toResponse).collect(Collectors.toList());
    }
}