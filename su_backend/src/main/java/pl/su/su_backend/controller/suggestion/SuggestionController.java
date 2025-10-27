package pl.su.su_backend.controller.suggestion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.suggestion.SuggestionRequestDto;
import pl.su.su_backend.dto.suggestion.SuggestionResponseDto;
import pl.su.su_backend.service.suggestion.SuggestionService;
import pl.su.su_backend.service.user.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
@Slf4j
public class SuggestionController {

    private final SuggestionService suggestionService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<SuggestionResponseDto> createSuggestion(@Valid @RequestBody SuggestionRequestDto dto,
                                                                @AuthenticationPrincipal User principal) {
        log.info("Creating suggestion by user {}", principal.getUsername());
        UUID userId = userService.getCurrentUserId(principal.getUsername());
        SuggestionResponseDto suggestion = suggestionService.createSuggestion(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(suggestion);
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'SUGGESTION_VIEW')")
    public ResponseEntity<List<SuggestionResponseDto>> getAllSuggestions(@AuthenticationPrincipal User principal) {
        log.info("Fetching all suggestions for user: {}", principal.getUsername());
        List<SuggestionResponseDto> suggestions = suggestionService.getAllSuggestions(principal.getUsername());
        return ResponseEntity.ok(suggestions);
    }


    @GetMapping("/user/{userId}")
    @PreAuthorize("hasPermission(null, 'SUGGESTION_VIEW')")
    public ResponseEntity<List<SuggestionResponseDto>> getUserSuggestions(@PathVariable UUID userId) {
        log.info("Fetching suggestions for user: {}", userId);
        List<SuggestionResponseDto> suggestions = suggestionService.getUserSuggestions(userId);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/{suggestionId}")
    @PreAuthorize("hasPermission(null, 'SUGGESTION_VIEW')")
    public ResponseEntity<SuggestionResponseDto> getSuggestionById(@PathVariable UUID suggestionId,
                                                                  @AuthenticationPrincipal User principal) {
        log.info("Fetching suggestion with ID: {} by user: {}", suggestionId, principal.getUsername());
        SuggestionResponseDto suggestion = suggestionService.getSuggestionById(suggestionId, principal.getUsername());
        return ResponseEntity.ok(suggestion);
    }

    @PutMapping("/{suggestionId}/approve")
    @PreAuthorize("hasPermission(null, 'SUGGESTION_APPROVE')")
    public ResponseEntity<SuggestionResponseDto> approveSuggestion(@PathVariable UUID suggestionId,
                                                                  @AuthenticationPrincipal User principal) {
        log.info("Approving suggestion {} by user {}", suggestionId, principal.getUsername());
        UUID approvedById = userService.getUserByEmail(principal.getUsername()).getId();
        SuggestionResponseDto suggestion = suggestionService.approveSuggestion(suggestionId, approvedById);
        return ResponseEntity.ok(suggestion);
    }

    @PutMapping("/{suggestionId}/reject")
    @PreAuthorize("hasPermission(null, 'SUGGESTION_APPROVE')")
    public ResponseEntity<SuggestionResponseDto> rejectSuggestion(@PathVariable UUID suggestionId,
                                                                @RequestParam String rejectionReason,
                                                                @AuthenticationPrincipal User principal) {
        log.info("Rejecting suggestion {} by user {} with reason: {}", suggestionId, principal.getUsername(), rejectionReason);
        UUID rejectedById = userService.getUserByEmail(principal.getUsername()).getId();
        SuggestionResponseDto suggestion = suggestionService.rejectSuggestion(suggestionId, rejectionReason, rejectedById);
        return ResponseEntity.ok(suggestion);
    }

    @PutMapping("/{suggestionId}")
    @PreAuthorize("hasPermission(null, 'SUGGESTION_EDIT')")
    public ResponseEntity<SuggestionResponseDto> updateSuggestion(@PathVariable UUID suggestionId,
                                                                @Valid @RequestBody SuggestionRequestDto dto,
                                                                @AuthenticationPrincipal User principal) {
        log.info("Updating suggestion {} by user {}", suggestionId, principal.getUsername());
        UUID userId = userService.getCurrentUserId(principal.getUsername());
        SuggestionResponseDto suggestion = suggestionService.updateSuggestion(suggestionId, dto, userId);
        return ResponseEntity.ok(suggestion);
    }

    @DeleteMapping("/{suggestionId}")
    @PreAuthorize("hasPermission(null, 'SUGGESTION_DELETE')")
    public ResponseEntity<Void> deleteSuggestion(@PathVariable UUID suggestionId,
                                               @AuthenticationPrincipal User principal) {
        log.info("Deleting suggestion {} by user {}", suggestionId, principal.getUsername());
        UUID userId = userService.getCurrentUserId(principal.getUsername());
        suggestionService.deleteSuggestion(suggestionId, userId);
        return ResponseEntity.noContent().build();
    }
}
