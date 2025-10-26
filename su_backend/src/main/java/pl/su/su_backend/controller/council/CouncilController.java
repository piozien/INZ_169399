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
public class CouncilController {

    private final CouncilService councilService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'COUNCIL_CREATE')")
    public ResponseEntity<CouncilResponseDto> createCouncil(@Valid @RequestBody CouncilRequestDto dto,
                                                            @AuthenticationPrincipal User principal) {
        log.info("Creating council: {} for academic year: {} by user: {}", dto.getName(), dto.getAcademicYear(), principal.getUsername());
        CouncilResponseDto council = councilService.createCouncil(dto, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(council);
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'COUNCIL_VIEW')")
    public ResponseEntity<List<CouncilResponseDto>> getCouncil(@AuthenticationPrincipal User principal) {
        log.info("Fetching councils for user: {}", principal.getUsername());
        List<CouncilResponseDto> councils = councilService.getCouncil(principal.getUsername());
        return ResponseEntity.ok(councils);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_VIEW')")
    public ResponseEntity<CouncilResponseDto> getCouncilById(@PathVariable UUID id,
                                                             @AuthenticationPrincipal User principal) {
        log.info("Fetching council by ID: {} for user: {}", id, principal.getUsername());
        CouncilResponseDto council = councilService.getCouncilById(id, principal.getUsername());
        return ResponseEntity.ok(council);
    }


    @PostMapping("/{councilId}/budget")
    @PreAuthorize("hasPermission(null, 'COUNCIL_BUDGET_CREATE')")
    public ResponseEntity<CouncilBudgetResponseDto> createBudget(@PathVariable UUID councilId,
                                                                 @Valid @RequestBody CouncilBudgetRequestDto dto,
                                                                 @AuthenticationPrincipal User principal) {
        log.info("Creating council budget for council {} by user: {}", councilId, principal.getUsername());
        CouncilBudgetResponseDto budget = councilService.createBudget(councilId, dto, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(budget);
    }

    @GetMapping("/{councilId}/budget")
    @PreAuthorize("hasPermission(null, 'COUNCIL_BUDGET_VIEW')")
    public ResponseEntity<CouncilBudgetResponseDto> getBudget(@PathVariable UUID councilId,
                                                             @AuthenticationPrincipal User principal) {
        log.info("Fetching council budget for council {} by user: {}", councilId, principal.getUsername());
        CouncilBudgetResponseDto budget = councilService.getBudget(councilId, principal.getUsername());
        return ResponseEntity.ok(budget);
    }

    @PostMapping("/{councilId}/transactions")
    @PreAuthorize("hasPermission(null, 'COUNCIL_TRANSACTION_CREATE')")
    public ResponseEntity<CouncilTransactionResponseDto> createTransaction(@PathVariable UUID councilId,
                                                                           @Valid @RequestBody CouncilTransactionRequestDto dto,
                                                                           @AuthenticationPrincipal User principal) {
        log.info("Creating council transaction for council {} by user: {}", councilId, principal.getUsername());
        CouncilTransactionResponseDto transaction = councilService.createTransaction(councilId, dto, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/budgets/{budgetId}/transactions")
    @PreAuthorize("hasPermission(null, 'COUNCIL_TRANSACTION_VIEW')")
    public ResponseEntity<List<CouncilTransactionResponseDto>> getTransactionsByBudget(@PathVariable UUID budgetId,
                                                                                       @AuthenticationPrincipal User principal) {
        log.info("Fetching transactions for budget: {} by user: {}", budgetId, principal.getUsername());
        List<CouncilTransactionResponseDto> transactions = councilService.getTransactionsByBudget(budgetId, principal.getUsername());
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/transactions/{transactionId}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_TRANSACTION_EDIT')")
    public ResponseEntity<CouncilTransactionResponseDto> updateTransaction(@PathVariable UUID transactionId,
                                                                           @Valid @RequestBody CouncilTransactionRequestDto dto,
                                                                           @AuthenticationPrincipal User principal) {
        log.info("Updating council transaction {} by user: {}", transactionId, principal.getUsername());
        CouncilTransactionResponseDto transaction = councilService.updateTransaction(transactionId, dto, principal.getUsername());
        return ResponseEntity.ok(transaction);
    }

    @DeleteMapping("/transactions/{transactionId}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_TRANSACTION_DELETE')")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID transactionId,
                                                  @AuthenticationPrincipal User principal) {
        log.info("Deleting council transaction {} by user: {}", transactionId, principal.getUsername());
        councilService.deleteTransaction(transactionId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/draft")
    @PreAuthorize("hasPermission(null, 'EVENT_VIEW_DRAFTS')")
    public ResponseEntity<List<EventResponseDto>> getDraftEvents(@AuthenticationPrincipal User principal) {
        log.info("Fetching draft events for SU by user: {}", principal.getUsername());
        List<EventResponseDto> events = councilService.getDraftEvents(principal.getUsername());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/pending")
    @PreAuthorize("hasPermission(null, 'EVENT_APPROVE')")
    public ResponseEntity<List<EventResponseDto>> getPendingEvents(@AuthenticationPrincipal User principal) {
        log.info("Fetching pending events for SU review by user: {}", principal.getUsername());
        List<EventResponseDto> events = councilService.getPendingEvents(principal.getUsername());
        return ResponseEntity.ok(events);
    }

    @PutMapping("/events/{eventId}/approve")
    @PreAuthorize("hasPermission(null, 'EVENT_APPROVE')")
    public ResponseEntity<EventResponseDto> approveEvent(@PathVariable UUID eventId,
                                                         @AuthenticationPrincipal User principal) {
        log.info("Approving event {} by SU user: {}", eventId, principal.getUsername());
        EventResponseDto event = councilService.approveEvent(eventId, principal.getUsername());
        return ResponseEntity.ok(event);
    }

    @PutMapping("/events/{eventId}/reject")
    @PreAuthorize("hasPermission(null, 'EVENT_APPROVE')")
    public ResponseEntity<EventResponseDto> rejectEvent(@PathVariable UUID eventId,
                                                        @AuthenticationPrincipal User principal) {
        log.info("Rejecting event {} by SU user: {}", eventId, principal.getUsername());
        EventResponseDto event = councilService.rejectEvent(eventId, principal.getUsername());
        return ResponseEntity.ok(event);
    }

    @PutMapping("/events/{eventId}/submit")
    @PreAuthorize("hasPermission(null, 'EVENT_EDIT')")
    public ResponseEntity<EventResponseDto> submitEventForApproval(@PathVariable UUID eventId,
                                                                   @AuthenticationPrincipal User principal) {
        log.info("Submitting event {} for approval by SU user: {}", eventId, principal.getUsername());
        EventResponseDto event = councilService.submitEventForApproval(eventId, principal.getUsername());
        return ResponseEntity.ok(event);
    }

    @PostMapping("/{councilId}/members/{userId}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_MEMBER_MANAGE')")
    public ResponseEntity<CouncilResponseDto> addMemberToCouncil(@PathVariable UUID councilId,
                                                                 @PathVariable UUID userId,
                                                                 @AuthenticationPrincipal User principal) {
        log.info("Adding member {} to council {} by user: {}", userId, councilId, principal.getUsername());
        CouncilResponseDto council = councilService.addMemberToCouncil(councilId, userId, principal.getUsername());
        return ResponseEntity.ok(council);
    }

    @DeleteMapping("/{councilId}/members/{userId}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_MEMBER_MANAGE')")
    public ResponseEntity<CouncilResponseDto> removeMemberFromCouncil(@PathVariable UUID councilId,
                                                                      @PathVariable UUID userId,
                                                                      @AuthenticationPrincipal User principal) {
        log.info("Removing member {} from council {} by user: {}", userId, councilId, principal.getUsername());
        CouncilResponseDto council = councilService.removeMemberFromCouncil(councilId, userId, principal.getUsername());
        return ResponseEntity.ok(council);
    }

    @GetMapping("/{councilId}/members")
    @PreAuthorize("hasPermission(null, 'COUNCIL_VIEW')")
    public ResponseEntity<List<UserResponseDto>> getCouncilMembers(@PathVariable UUID councilId,
                                                                   @AuthenticationPrincipal User principal) {
        log.info("Fetching members of council {} by user: {}", councilId, principal.getUsername());
        List<UserResponseDto> members = councilService.getCouncilMembers(councilId, principal.getUsername());
        return ResponseEntity.ok(members);
    }

    @PostMapping("/join/{joinCode}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_JOIN')")
    public ResponseEntity<CouncilResponseDto> joinCouncilByCode(
            @PathVariable String joinCode,
            @AuthenticationPrincipal User principal) {

        log.info("User {} attempting to join council with code: {}", principal.getUsername(), joinCode);

        CouncilResponseDto response = councilService.joinCouncilByCode(joinCode, principal.getUsername());

        return ResponseEntity.ok(response);
    }

}
