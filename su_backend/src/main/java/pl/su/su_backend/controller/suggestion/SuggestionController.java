package pl.su.su_backend.controller.suggestion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.suggestion.SuggestionRequestDto;
import pl.su.su_backend.dto.suggestion.SuggestionResponseDto;
import pl.su.su_backend.service.auth.AuthenticationService;
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
    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<SuggestionResponseDto> createSuggestion(@Valid @RequestBody SuggestionRequestDto dto,
                                                                @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Creating suggestion by user {}", email);
        UUID userId = userService.getCurrentUserId(email);
        SuggestionResponseDto suggestion = suggestionService.createSuggestion(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(suggestion);
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'SUGGESTION_VIEW')")
    public ResponseEntity<List<SuggestionResponseDto>> getAllSuggestions(@AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching all suggestions for user: {}", email);
        List<SuggestionResponseDto> suggestions = suggestionService.getAllSuggestions(email);
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
                                                                  @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching suggestion with ID: {} by user: {}", suggestionId, email);
        SuggestionResponseDto suggestion = suggestionService.getSuggestionById(suggestionId, email);
        return ResponseEntity.ok(suggestion);
    }

    @PutMapping("/{suggestionId}/approve")
    @PreAuthorize("hasPermission(null, 'SUGGESTION_APPROVE')")
    public ResponseEntity<SuggestionResponseDto> approveSuggestion(@PathVariable UUID suggestionId,
                                                                  @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Approving suggestion {} by user {}", suggestionId, email);
        UUID approvedById = userService.getUserByEmail(email).getId();
        SuggestionResponseDto suggestion = suggestionService.approveSuggestion(suggestionId, approvedById);
        return ResponseEntity.ok(suggestion);
    }

    @PutMapping("/{suggestionId}/reject")
    @PreAuthorize("hasPermission(null, 'SUGGESTION_APPROVE')")
    public ResponseEntity<SuggestionResponseDto> rejectSuggestion(@PathVariable UUID suggestionId,
                                                                @RequestParam String rejectionReason,
                                                                @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Rejecting suggestion {} by user {} with reason: {}", suggestionId, email, rejectionReason);
        UUID rejectedById = userService.getUserByEmail(email).getId();
        SuggestionResponseDto suggestion = suggestionService.rejectSuggestion(suggestionId, rejectionReason, rejectedById);
        return ResponseEntity.ok(suggestion);
    }

    @PutMapping("/{suggestionId}")
    @PreAuthorize("hasPermission(null, 'SUGGESTION_EDIT')")
    public ResponseEntity<SuggestionResponseDto> updateSuggestion(@PathVariable UUID suggestionId,
                                                                @Valid @RequestBody SuggestionRequestDto dto,
                                                                @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Updating suggestion {} by user {}", suggestionId, email);
        UUID userId = userService.getCurrentUserId(email);
        SuggestionResponseDto suggestion = suggestionService.updateSuggestion(suggestionId, dto, userId);
        return ResponseEntity.ok(suggestion);
    }

    @DeleteMapping("/{suggestionId}")
    @PreAuthorize("hasPermission(null, 'SUGGESTION_DELETE')")
    public ResponseEntity<Void> deleteSuggestion(@PathVariable UUID suggestionId,
                                               @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Deleting suggestion {} by user {}", suggestionId, email);
        UUID userId = userService.getCurrentUserId(email);
        suggestionService.deleteSuggestion(suggestionId, userId);
        return ResponseEntity.noContent().build();
    }
}
