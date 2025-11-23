package pl.su.su_backend.controller.council;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.budget.CouncilBudgetRequestDto;
import pl.su.su_backend.dto.budget.CouncilBudgetResponseDto;
import pl.su.su_backend.dto.budget.CouncilTransactionRequestDto;
import pl.su.su_backend.dto.budget.CouncilTransactionResponseDto;
import pl.su.su_backend.dto.council.CouncilMemberDto;
import pl.su.su_backend.dto.council.CouncilMemberMapper;
import pl.su.su_backend.dto.council.CouncilRequestDto;
import pl.su.su_backend.dto.council.CouncilResponseDto;
import pl.su.su_backend.dto.event.EventResponseDto;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.service.auth.AuthenticationService;
import pl.su.su_backend.service.council.CouncilMemberService;
import pl.su.su_backend.service.council.CouncilService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/council")
@RequiredArgsConstructor
@Slf4j
public class CouncilController {

    private final CouncilService councilService;
    private final CouncilMemberService councilMemberService;
    private final AuthenticationService authenticationService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'COUNCIL_CREATE')")
    public ResponseEntity<CouncilResponseDto> createCouncil(@Valid @RequestBody CouncilRequestDto dto,
                                                            @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Creating council: {} for academic year: {} by user: {}", dto.getName(), dto.getAcademicYear(), email);
        CouncilResponseDto council = councilService.createCouncil(dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(council);
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'COUNCIL_VIEW')")
    public ResponseEntity<List<CouncilResponseDto>> getCouncil(@AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching councils for user: {}", email);
        List<CouncilResponseDto> councils = councilService.getCouncil(email);
        return ResponseEntity.ok(councils);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_VIEW')")
    public ResponseEntity<CouncilResponseDto> getCouncilById(@PathVariable UUID id,
                                                             @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching council by ID: {} for user: {}", id, email);
        CouncilResponseDto council = councilService.getCouncilById(id, email);
        return ResponseEntity.ok(council);
    }


    @PostMapping("/{councilId}/budget")
    @PreAuthorize("hasPermission(null, 'COUNCIL_BUDGET_CREATE')")
    public ResponseEntity<CouncilBudgetResponseDto> createBudget(@PathVariable UUID councilId,
                                                                 @Valid @RequestBody CouncilBudgetRequestDto dto,
                                                                 @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Creating council budget for council {} by user: {}", councilId, email);
        CouncilBudgetResponseDto budget = councilService.createBudget(councilId, dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(budget);
    }

    @GetMapping("/{councilId}/budget")
    @PreAuthorize("hasPermission(null, 'COUNCIL_BUDGET_VIEW')")
    public ResponseEntity<CouncilBudgetResponseDto> getBudget(@PathVariable UUID councilId,
                                                             @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching council budget for council {} by user: {}", councilId, email);
        CouncilBudgetResponseDto budget = councilService.getBudget(councilId, email);
        return ResponseEntity.ok(budget);
    }

    @PostMapping("/{councilId}/transactions")
    @PreAuthorize("hasPermission(null, 'COUNCIL_TRANSACTION_CREATE')")
    public ResponseEntity<CouncilTransactionResponseDto> createTransaction(@PathVariable UUID councilId,
                                                                           @Valid @RequestBody CouncilTransactionRequestDto dto,
                                                                           @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Creating council transaction for council {} by user: {}", councilId, email);
        CouncilTransactionResponseDto transaction = councilService.createTransaction(councilId, dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/budgets/{budgetId}/transactions")
    @PreAuthorize("hasPermission(null, 'COUNCIL_TRANSACTION_VIEW')")
    public ResponseEntity<List<CouncilTransactionResponseDto>> getTransactionsByBudget(@PathVariable UUID budgetId,
                                                                                       @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching transactions for budget: {} by user: {}", budgetId, email);
        List<CouncilTransactionResponseDto> transactions = councilService.getTransactionsByBudget(budgetId, email);
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/transactions/{transactionId}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_TRANSACTION_EDIT')")
    public ResponseEntity<CouncilTransactionResponseDto> updateTransaction(@PathVariable UUID transactionId,
                                                                           @Valid @RequestBody CouncilTransactionRequestDto dto,
                                                                           @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Updating council transaction {} by user: {}", transactionId, email);
        CouncilTransactionResponseDto transaction = councilService.updateTransaction(transactionId, dto, email);
        return ResponseEntity.ok(transaction);
    }

    @DeleteMapping("/transactions/{transactionId}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_TRANSACTION_DELETE')")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID transactionId,
                                                  @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Deleting council transaction {} by user: {}", transactionId, email);
        councilService.deleteTransaction(transactionId, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/draft")
    @PreAuthorize("hasPermission(null, 'EVENT_VIEW_DRAFTS')")
    public ResponseEntity<List<EventResponseDto>> getDraftEvents(@AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching draft events for SU by user: {}", email);
        List<EventResponseDto> events = councilService.getDraftEvents(email);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/pending")
    @PreAuthorize("hasPermission(null, 'EVENT_APPROVE')")
    public ResponseEntity<List<EventResponseDto>> getPendingEvents(@AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching pending events for SU review by user: {}", email);
        List<EventResponseDto> events = councilService.getPendingEvents(email);
        return ResponseEntity.ok(events);
    }

    @PutMapping("/events/{eventId}/approve")
    @PreAuthorize("hasPermission(null, 'EVENT_APPROVE')")
    public ResponseEntity<EventResponseDto> approveEvent(@PathVariable UUID eventId,
                                                         @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Approving event {} by SU user: {}", eventId, email);
        EventResponseDto event = councilService.approveEvent(eventId, email);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/events/{eventId}/reject")
    @PreAuthorize("hasPermission(null, 'EVENT_APPROVE')")
    public ResponseEntity<EventResponseDto> rejectEvent(@PathVariable UUID eventId,
                                                        @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Rejecting event {} by SU user: {}", eventId, email);
        EventResponseDto event = councilService.rejectEvent(eventId, email);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/events/{eventId}/submit")
    @PreAuthorize("hasPermission(null, 'EVENT_EDIT')")
    public ResponseEntity<EventResponseDto> submitEventForApproval(@PathVariable UUID eventId,
                                                                   @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Submitting event {} for approval by SU user: {}", eventId, email);
        EventResponseDto event = councilService.submitEventForApproval(eventId, email);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/{councilId}/members/{userId}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_MEMBER_MANAGE')")
    public ResponseEntity<CouncilResponseDto> addMemberToCouncil(@PathVariable UUID councilId,
                                                                 @PathVariable UUID userId,
                                                                 @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Adding member {} to council {} by user: {}", userId, councilId, email);
        CouncilResponseDto council = councilService.addMemberToCouncil(councilId, userId, email);
        return ResponseEntity.ok(council);
    }

    @DeleteMapping("/{councilId}/members/{userId}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_MEMBER_MANAGE')")
    public ResponseEntity<CouncilResponseDto> removeMemberFromCouncil(@PathVariable UUID councilId,
                                                                      @PathVariable UUID userId,
                                                                      @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Removing member {} from council {} by user: {}", userId, councilId, email);
        CouncilResponseDto council = councilService.removeMemberFromCouncil(councilId, userId, email);
        return ResponseEntity.ok(council);
    }

    @GetMapping("/{councilId}/members")
    @PreAuthorize("hasPermission(null, 'COUNCIL_VIEW')")
    public ResponseEntity<List<CouncilMemberDto>> getCouncilMembers(@PathVariable UUID councilId,
                                                                     @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching members of council {} by user: {}", councilId, email);
        List<CouncilMember> members = councilMemberService.getCouncilMembers(councilId, email);
        List<CouncilMemberDto> memberDtos = members.stream()
                .map(CouncilMemberMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(memberDtos);
    }

    @PostMapping("/join/{joinCode}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_JOIN')")
    public ResponseEntity<CouncilResponseDto> joinCouncilByCode(
            @PathVariable String joinCode,
            @AuthenticationPrincipal Object principal) {

        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("User {} attempting to join council with code: {}", email, joinCode);

        CouncilResponseDto response = councilService.joinCouncilByCode(joinCode, email);

        return ResponseEntity.ok(response);
    }

}
