package pl.su.su_backend.service.council;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;
import pl.su.su_backend.dto.budget.*;
import pl.su.su_backend.dto.council.CouncilRequestDto;
import pl.su.su_backend.dto.council.CouncilResponseDto;
import pl.su.su_backend.dto.council.CouncilMapper;
import pl.su.su_backend.dto.event.EventResponseDto;
import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.dto.user.UserMapper;
import pl.su.su_backend.model.budget.CouncilBudget;
import pl.su.su_backend.model.budget.CouncilTransaction;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.budget.CouncilBudgetRepository;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.repositories.budget.CouncilTransactionRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.service.event.EventService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CouncilService {

    private final CouncilRepository councilRepository;
    private final CouncilBudgetRepository councilBudgetRepository;
    private final CouncilTransactionRepository councilTransactionRepository;
    private final UsersRepository usersRepository;
    private final ActivityLogService activityLogService;
    private final EventService eventService;
    private final PermissionService permissionService;

    @Transactional(readOnly = true)
    public List<CouncilResponseDto> getCouncil(String currentUserEmail) {
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));

        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        if (permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_VIEW_ALL)) {
            List<Council> allCouncils = councilRepository.findAll();
            return allCouncils.stream()
                    .map(CouncilMapper::toResponseDto)
                    .toList();
        }

        List<Council> userCouncils = councilRepository.findAll().stream()
                .filter(council -> council.getMembers().contains(currentUser))
                .toList();
        
        return userCouncils.stream()
                .map(CouncilMapper::toResponseDto)
                .toList();
    }

    public CouncilBudgetResponseDto createBudget(UUID councilId, CouncilBudgetRequestDto dto, String currentUserEmail) {
        log.info("Creating council budget for council {} by user: {}", councilId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_BUDGET_CREATE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Council not found"));
        
        String year = dto.getYear() != null ? dto.getYear() : String.valueOf(java.time.LocalDateTime.now().getYear());
        if (councilBudgetRepository.findByCouncil_IdAndYear(council.getId(), year).isPresent()) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Budget for council and year " + year + " already exists");
        }
        
        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year(year)
                .initialAmount(dto.getInitialAmount() != null ? dto.getInitialAmount() : BigDecimal.ZERO)
                .createdBy(user)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        
        CouncilBudget savedBudget = councilBudgetRepository.save(budget);
        
        activityLogService.log(user.getId(), ActionType.BUDGET_CREATE, "Created council budget for year: " + dto.getYear());
        
        return CouncilBudgetMapper.toResponse(savedBudget);
    }

    public CouncilBudgetResponseDto getBudget(UUID councilId, String currentUserEmail) {
        log.info("Fetching council budget for council {} by user: {}", councilId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_BUDGET_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        CouncilBudget budget = councilBudgetRepository.findByCouncil_IdOrderByYearDesc(councilId)
                .stream()
                .findFirst()
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Budget not found for council"));
        
        return CouncilBudgetMapper.toResponse(budget);
    }

    public CouncilTransactionResponseDto createTransaction(UUID councilId, CouncilTransactionRequestDto dto, String currentUserEmail) {
        log.info("Creating council transaction for council {} by user: {}", councilId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_TRANSACTION_CREATE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        CouncilBudget budget = councilBudgetRepository.findById(dto.getBudgetId())
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Budget not found"));
        
        if (!budget.getCouncil().getId().equals(councilId)) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Budget does not belong to specified council");
        }
        
        CouncilTransaction transaction = CouncilTransactionMapper.toEntity(dto, budget, user);
        CouncilTransaction savedTransaction = councilTransactionRepository.save(transaction);
        
        updateBudgetBalance(dto.getBudgetId());
        
        activityLogService.log(user.getId(), ActionType.TRANSACTION_CREATE, "Created council transaction: " + dto.getDescription());
        
        return CouncilTransactionMapper.toResponse(savedTransaction);
    }

    public CouncilTransactionResponseDto updateTransaction(UUID transactionId, CouncilTransactionRequestDto dto, String currentUserEmail) {
        log.info("Updating council transaction {} by user: {}", transactionId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_TRANSACTION_EDIT)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        CouncilTransaction transaction = councilTransactionRepository.findById(transactionId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Transaction not found"));
        
        transaction.setType(dto.getType());
        transaction.setAmount(dto.getAmount());
        transaction.setDescription(dto.getDescription());
        transaction.setDate(dto.getDate());
        
        CouncilTransaction updatedTransaction = councilTransactionRepository.save(transaction);
        
        updateBudgetBalance(transaction.getBudget().getId());
        
        activityLogService.log(user.getId(), ActionType.TRANSACTION_EDIT, "Updated council transaction: "
         + dto.getDescription());
        
        return CouncilTransactionMapper.toResponse(updatedTransaction);
    }

    public void deleteTransaction(UUID transactionId, String currentUserEmail) {
        log.info("Deleting council transaction {} by user: {}", transactionId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_TRANSACTION_DELETE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        CouncilTransaction transaction = councilTransactionRepository.findById(transactionId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR,
                 "Transaction not found"));
        
        UUID budgetId = transaction.getBudget().getId();
        councilTransactionRepository.delete(transaction);
        
        updateBudgetBalance(budgetId);
        
        activityLogService.log(user.getId(), ActionType.TRANSACTION_DELETE, "Deleted council transaction: "
         + transaction.getDescription());
    }

    public List<CouncilTransactionResponseDto> getTransactionsByBudget(UUID budgetId, String currentUserEmail) {
        log.info("Fetching transactions for budget: {} by user: {}", budgetId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_TRANSACTION_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        List<CouncilTransaction> transactions = councilTransactionRepository.findByBudgetId(budgetId);
        return CouncilTransactionMapper.toResponseList(transactions);
    }

    public List<EventResponseDto> getDraftEvents(String currentUserEmail) {
        log.info("Fetching draft events for SU by user: {}", currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        return eventService.getDraftEventsForSU(currentUserEmail);
    }

    public List<EventResponseDto> getPendingEvents(String currentUserEmail) {
        log.info("Fetching pending events for SU review by user: {}", currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        return eventService.getPendingEvents();
    }

    public EventResponseDto approveEvent(UUID eventId, String currentUserEmail) {
        log.info("Approving event {} by SU user: {}", eventId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_APPROVE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        return eventService.approveEvent(eventId, user.getId());
    }

    public EventResponseDto rejectEvent(UUID eventId, String currentUserEmail) {
        log.info("Rejecting event {} by SU user: {}", eventId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_APPROVE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        return eventService.rejectEvent(eventId, user.getId());
    }

    public EventResponseDto submitEventForApproval(UUID eventId, String currentUserEmail) {
        log.info("Submitting event {} for approval by SU user: {}", eventId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_CREATE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        return eventService.submitEventForApproval(eventId, user.getId());
    }

    public CouncilResponseDto addMemberToCouncil(UUID councilId, UUID userId, String currentUserEmail) {
        log.info("Adding member {} to council {} by user: {}", userId, councilId, currentUserEmail);
        
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_MEMBER_MANAGE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Council not found"));
        
        Users member = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!council.getMembers().contains(member)) {
            council.getMembers().add(member);
            council = councilRepository.save(council);
            activityLogService.log(currentUser.getId(), ActionType.USER_UPDATED,
                    "Added member to council: " + member.getFullName());
        }
        
        return CouncilMapper.toResponseDto(council);
    }

    public CouncilResponseDto removeMemberFromCouncil(UUID councilId, UUID userId, String currentUserEmail) {
        log.info("Removing member {} from council {} by user: {}", userId, councilId, currentUserEmail);
        
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_MEMBER_MANAGE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Council not found"));
        
        Users member = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (council.getMembers().contains(member)) {
            council.getMembers().remove(member);
            council = councilRepository.save(council);
            activityLogService.log(currentUser.getId(), ActionType.USER_UPDATED,
                    "Removed member from council: " + member.getFullName());
        }
        
        return CouncilMapper.toResponseDto(council);
    }

    public List<UserResponseDto> getCouncilMembers(UUID councilId, String currentUserEmail) {
        log.info("Fetching members of council {} by user: {}", councilId, currentUserEmail);
        
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Council not found"));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        return council.getMembers().stream()
                .map(UserMapper::toResponseDto)
                .collect(java.util.stream.Collectors.toList());
    }


    public CouncilResponseDto createCouncil(CouncilRequestDto dto, String currentUserEmail) {
        log.info("Creating council: {} for academic year: {} by user: {}", dto.getName(), dto.getAcademicYear(), currentUserEmail);
        
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_CREATE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        Council council = CouncilMapper.toEntity(dto);
        
        String joinCode = generateJoinCodeForCouncil(dto.getAcademicYear());
        council.setJoinCode(joinCode);
        
        council = councilRepository.save(council);
        
        activityLogService.log(currentUser.getId(), ActionType.COUNCIL_CREATE,
                "Created council: " + dto.getName() + " for academic year: " + dto.getAcademicYear() + " with join code: " + joinCode);
        
        return CouncilMapper.toResponseDto(council);
    }

    private String generateJoinCodeForCouncil(String academicYear) {
        String year = academicYear.split("/")[0]; // "2024/2025" -> "2024"
        String prefix = "SU" + year;
        
        String code;
        do {
            int randomNumber = new Random().nextInt(10000); // 0-9999
            code = prefix + String.format("%04d", randomNumber);
        } while (councilRepository.findByJoinCode(code).isPresent());
        
        return code;
    }

    @Transactional
    public void updateBudgetBalance(UUID budgetId) {
        log.info("Updating balance for council budget: {}", budgetId);
        
        CouncilBudget budget = councilBudgetRepository.findById(budgetId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Budget not found: "
                 + budgetId));

        BigDecimal newBalance = getBigDecimal(budget);

        budget.setBalance(newBalance);
        councilBudgetRepository.save(budget);
        
        log.info("Council budget balance updated to: {}", newBalance);
    }

    private static BigDecimal getBigDecimal(CouncilBudget budget) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (CouncilTransaction transaction : budget.getTransactions()) {
            if (transaction.getType() == pl.su.su_backend.model.enums.TransactionType.INCOME) {
                totalIncome = totalIncome.add(transaction.getAmount());
            } else if (transaction.getType() == pl.su.su_backend.model.enums.TransactionType.EXPENSE) {
                totalExpenses = totalExpenses.add(transaction.getAmount());
            }
        }

        BigDecimal newBalance = (budget.getInitialAmount() != null ? budget.getInitialAmount() : BigDecimal.ZERO)
                .add(totalIncome)
                .subtract(totalExpenses);
        return newBalance;
    }


    public CouncilResponseDto joinCouncilByCode(String joinCode, String currentUserEmail) {
        log.info("User {} attempting to join council with code: {}", currentUserEmail, joinCode);
        
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_JOIN)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied - no permission to join council");
        }
        
        Council council = councilRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid join code"));
        
        if (!council.getIsActive()) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Council is not active");
        }
        
        if (council.getMembers().contains(currentUser)) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "User is already a member of this council");
        }

        council.getMembers().add(currentUser);
        council = councilRepository.save(council);
        
        activityLogService.log(currentUser.getId(), ActionType.USER_UPDATED,
                "Joined council using code: " + joinCode + " (pending admin approval)");
        
        log.info("User {} successfully joined council {} - pending admin approval", currentUserEmail, council.getName());
        
        return CouncilMapper.toResponseDto(council);
    }
    
    public CouncilResponseDto getCouncilById(UUID id, String currentUserEmail) {
        log.info("Fetching council by ID: {} for user: {}", id, currentUserEmail);
        
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        Council council = councilRepository.findById(id)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "Council not found"));
        
        return CouncilMapper.toResponseDto(council);
    }

}
