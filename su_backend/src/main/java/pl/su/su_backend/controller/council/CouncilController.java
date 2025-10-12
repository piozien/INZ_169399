package pl.su.su_backend.controller.council;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.budget.CouncilBudgetRequestDto;
import pl.su.su_backend.dto.budget.CouncilBudgetResponseDto;
import pl.su.su_backend.dto.budget.CouncilTransactionRequestDto;
import pl.su.su_backend.dto.budget.CouncilTransactionResponseDto;
import pl.su.su_backend.dto.council.CouncilRequestDto;
import pl.su.su_backend.dto.council.CouncilResponseDto;
import pl.su.su_backend.dto.event.EventResponseDto;
import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.service.council.CouncilService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/council")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CouncilController {

    private final CouncilService councilService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilResponseDto> createCouncil(@Valid @RequestBody CouncilRequestDto dto,
                                                          @AuthenticationPrincipal User principal) {
        log.info("Creating council: {} for academic year: {} by user: {}", dto.getName(), dto.getAcademicYear(), principal.getUsername());
        try {
            CouncilResponseDto council = councilService.createCouncil(dto, principal.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(council);
        } catch (Exception e) {
            log.error("Failed to create council: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilResponseDto> getCouncil(@AuthenticationPrincipal User principal) {
        log.info("Fetching council by user: {}", principal.getUsername());
        try {
            CouncilResponseDto council = councilService.getCouncil();
            return ResponseEntity.ok(council);
        } catch (Exception e) {
            log.error("Failed to fetch council: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/budgets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilBudgetResponseDto> createBudget(@Valid @RequestBody CouncilBudgetRequestDto dto,
                                                               @AuthenticationPrincipal User principal) {
        log.info("Creating council budget by user: {}", principal.getUsername());
        try {
            CouncilBudgetResponseDto budget = councilService.createBudget(dto, principal.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(budget);
        } catch (Exception e) {
            log.error("Failed to create council budget: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/budgets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CouncilBudgetResponseDto>> getAllBudgets(@AuthenticationPrincipal User principal) {
        log.info("Fetching all council budgets for user: {}", principal.getUsername());
        try {
            List<CouncilBudgetResponseDto> budgets = councilService.getAllBudgets(principal.getUsername());
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            log.error("Failed to fetch council budgets: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilTransactionResponseDto> createTransaction(@Valid @RequestBody CouncilTransactionRequestDto dto,
                                                                         @AuthenticationPrincipal User principal) {
        log.info("Creating council transaction by user: {}", principal.getUsername());
        try {
            CouncilTransactionResponseDto transaction = councilService.createTransaction(dto, principal.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (Exception e) {
            log.error("Failed to create council transaction: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/budgets/{budgetId}/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CouncilTransactionResponseDto>> getTransactionsByBudget(@PathVariable UUID budgetId,
                                                                                      @AuthenticationPrincipal User principal) {
        log.info("Fetching transactions for budget: {} by user: {}", budgetId, principal.getUsername());
        try {
            List<CouncilTransactionResponseDto> transactions = councilService.getTransactionsByBudget(budgetId, principal.getUsername());
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Failed to fetch transactions for budget {}: {}", budgetId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/events/draft")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponseDto>> getDraftEvents(@AuthenticationPrincipal User principal) {
        log.info("Fetching draft events for SU by user: {}", principal.getUsername());
        try {
            List<EventResponseDto> events = councilService.getDraftEvents(principal.getUsername());
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Failed to fetch draft events: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/events/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponseDto>> getPendingEvents(@AuthenticationPrincipal User principal) {
        log.info("Fetching pending events for SU review by user: {}", principal.getUsername());
        try {
            List<EventResponseDto> events = councilService.getPendingEvents(principal.getUsername());
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Failed to fetch pending events: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/events/{eventId}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDto> approveEvent(@PathVariable UUID eventId,
                                                       @AuthenticationPrincipal User principal) {
        log.info("Approving event {} by SU user: {}", eventId, principal.getUsername());
        try {
            EventResponseDto event = councilService.approveEvent(eventId, principal.getUsername());
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            log.error("Failed to approve event {}: {}", eventId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/events/{eventId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDto> rejectEvent(@PathVariable UUID eventId,
                                                      @AuthenticationPrincipal User principal) {
        log.info("Rejecting event {} by SU user: {}", eventId, principal.getUsername());
        try {
            EventResponseDto event = councilService.rejectEvent(eventId, principal.getUsername());
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            log.error("Failed to reject event {}: {}", eventId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/events/{eventId}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDto> submitEventForApproval(@PathVariable UUID eventId,
                                                                 @AuthenticationPrincipal User principal) {
        log.info("Submitting event {} for approval by SU user: {}", eventId, principal.getUsername());
        try {
            EventResponseDto event = councilService.submitEventForApproval(eventId, principal.getUsername());
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            log.error("Failed to submit event {} for approval: {}", eventId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{councilId}/members/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilResponseDto> addMemberToCouncil(@PathVariable UUID councilId,
                                                               @PathVariable UUID userId,
                                                               @AuthenticationPrincipal User principal) {
        log.info("Adding member {} to council {} by user: {}", userId, councilId, principal.getUsername());
        try {
            CouncilResponseDto council = councilService.addMemberToCouncil(councilId, userId, principal.getUsername());
            return ResponseEntity.ok(council);
        } catch (Exception e) {
            log.error("Failed to add member {} to council {}: {}", userId, councilId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{councilId}/members/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilResponseDto> removeMemberFromCouncil(@PathVariable UUID councilId,
                                                                    @PathVariable UUID userId,
                                                                    @AuthenticationPrincipal User principal) {
        log.info("Removing member {} from council {} by user: {}", userId, councilId, principal.getUsername());
        try {
            CouncilResponseDto council = councilService.removeMemberFromCouncil(councilId, userId, principal.getUsername());
            return ResponseEntity.ok(council);
        } catch (Exception e) {
            log.error("Failed to remove member {} from council {}: {}", userId, councilId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{councilId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponseDto>> getCouncilMembers(@PathVariable UUID councilId,
                                                                  @AuthenticationPrincipal User principal) {
        log.info("Fetching members of council {} by user: {}", councilId, principal.getUsername());
        try {
            List<UserResponseDto> members = councilService.getCouncilMembers(councilId, principal.getUsername());
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            log.error("Failed to fetch members of council {}: {}", councilId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
