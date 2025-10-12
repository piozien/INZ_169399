package pl.su.su_backend.service.council;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.List;
import java.util.UUID;

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
    public CouncilResponseDto getCouncil() {
        List<Council> councils = councilRepository.findAll();
        if (councils.isEmpty()) {
            throw new RuntimeException("No council found. Please create a council first.");
        }
        return CouncilMapper.toResponseDto(councils.getFirst());
    }

    @Transactional(readOnly = true)
    protected Council getCouncilEntity() {
        List<Council> councils = councilRepository.findAll();
        if (councils.isEmpty()) {
            throw new RuntimeException("No council found. Please create a council first.");
        }
        return councils.getFirst();
    }

    public CouncilBudgetResponseDto createBudget(CouncilBudgetRequestDto dto, String currentUserEmail) {
        log.info("Creating council budget by user: {}", currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_BUDGET_CREATE)) {
            throw new RuntimeException("Access denied: User must have council budget creation permission");
        }
        
        Council council = getCouncilEntity();
        CouncilBudget budget = CouncilBudgetMapper.toEntity(dto, council, user);
        CouncilBudget savedBudget = councilBudgetRepository.save(budget);
        
        activityLogService.log(user.getId(), ActionType.BUDGET_CREATE, "Created council budget for year: " + dto.getYear());
        
        return CouncilBudgetMapper.toResponse(savedBudget);
    }

    public List<CouncilBudgetResponseDto> getAllBudgets(String currentUserEmail) {
        log.info("Fetching all council budgets for user: {}", currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_BUDGET_VIEW)) {
            throw new RuntimeException("Access denied: User must have council budget viewing permission");
        }
        
        List<CouncilBudget> budgets = councilBudgetRepository.findAll();
        return CouncilBudgetMapper.toResponseList(budgets);
    }

    public CouncilTransactionResponseDto createTransaction(CouncilTransactionRequestDto dto, String currentUserEmail) {
        log.info("Creating council transaction by user: {}", currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_TRANSACTION_CREATE)) {
            throw new RuntimeException("Access denied: User must have council transaction creation permission");
        }
        
        CouncilBudget budget = councilBudgetRepository.findById(dto.getBudgetId())
                .orElseThrow(() -> new RuntimeException("Budget not found: " + dto.getBudgetId()));
        
        CouncilTransaction transaction = CouncilTransactionMapper.toEntity(dto, budget, user);
        CouncilTransaction savedTransaction = councilTransactionRepository.save(transaction);
        
        activityLogService.log(user.getId(), ActionType.TRANSACTION_CREATE, "Created council transaction: " + dto.getDescription());
        
        return CouncilTransactionMapper.toResponse(savedTransaction);
    }

    public List<CouncilTransactionResponseDto> getTransactionsByBudget(UUID budgetId, String currentUserEmail) {
        log.info("Fetching transactions for budget: {} by user: {}", budgetId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.COUNCIL_TRANSACTION_VIEW)) {
            throw new RuntimeException("Access denied: User must have council transaction viewing permission");
        }
        
        List<CouncilTransaction> transactions = councilTransactionRepository.findByBudgetId(budgetId);
        return CouncilTransactionMapper.toResponseList(transactions);
    }

    public List<EventResponseDto> getDraftEvents(String currentUserEmail) {
        log.info("Fetching draft events for SU by user: {}", currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_VIEW)) {
            throw new RuntimeException("Access denied: User must be a member of SU");
        }
        
        return eventService.getDraftEventsForSU(currentUserEmail);
    }

    public List<EventResponseDto> getPendingEvents(String currentUserEmail) {
        log.info("Fetching pending events for SU review by user: {}", currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_VIEW)) {
            throw new RuntimeException("Access denied: User must be a member of SU");
        }
        
        return eventService.getPendingEvents();
    }

    public EventResponseDto approveEvent(UUID eventId, String currentUserEmail) {
        log.info("Approving event {} by SU user: {}", eventId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_APPROVE)) {
            throw new RuntimeException("Access denied: User must be a member of SU");
        }
        
        return eventService.approveEvent(eventId, user.getId());
    }

    public EventResponseDto rejectEvent(UUID eventId, String currentUserEmail) {
        log.info("Rejecting event {} by SU user: {}", eventId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_APPROVE)) {
            throw new RuntimeException("Access denied: User must be a member of SU");
        }
        
        return eventService.rejectEvent(eventId, user.getId());
    }

    public EventResponseDto submitEventForApproval(UUID eventId, String currentUserEmail) {
        log.info("Submitting event {} for approval by SU user: {}", eventId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_CREATE)) {
            throw new RuntimeException("Access denied: User must be a member of SU");
        }
        
        return eventService.submitEventForApproval(eventId, user.getId());
    }

    public CouncilResponseDto addMemberToCouncil(UUID councilId, UUID userId, String currentUserEmail) {
        log.info("Adding member {} to council {} by user: {}", userId, councilId, currentUserEmail);
        
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_MEMBER_MANAGE)) {
            throw new RuntimeException("Access denied: User must have council member management permission");
        }
        
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new RuntimeException("Council not found: " + councilId));
        
        Users member = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
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
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_MEMBER_MANAGE)) {
            throw new RuntimeException("Access denied: User must have council member management permission");
        }
        
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new RuntimeException("Council not found: " + councilId));
        
        Users member = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
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
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new RuntimeException("Council not found: " + councilId));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_VIEW)) {
            throw new RuntimeException("Access denied: User must have council viewing permission");
        }
        
        return council.getMembers().stream()
                .map(UserMapper::toResponseDto)
                .collect(java.util.stream.Collectors.toList());
    }


    public CouncilResponseDto createCouncil(CouncilRequestDto dto, String currentUserEmail) {
        log.info("Creating council: {} for academic year: {} by user: {}", dto.getName(), dto.getAcademicYear(), currentUserEmail);
        
        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_CREATE)) {
            throw new RuntimeException("Access denied: User must have council creation permission");
        }
        
        Council council = CouncilMapper.toEntity(dto);
        council = councilRepository.save(council);
        
        activityLogService.log(currentUser.getId(), ActionType.COUNCIL_CREATE,
                "Created council: " + dto.getName() + " for academic year: " + dto.getAcademicYear());
        
        return CouncilMapper.toResponseDto(council);
    }

}
