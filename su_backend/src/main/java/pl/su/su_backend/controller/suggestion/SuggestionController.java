package pl.su.su_backend.controller.suggestion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuggestionResponseDto> createSuggestion(
            @Valid @RequestBody SuggestionRequestDto dto,
            @AuthenticationPrincipal Object principal) {

        String email = getCurrentUserEmail(principal);
        log.info("Creating suggestion by user {}", email);

        UUID userId = userService.getCurrentUserId(email);

        SuggestionResponseDto suggestion = suggestionService.createSuggestion(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(suggestion);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SuggestionResponseDto>> getAllSuggestions(
            @AuthenticationPrincipal Object principal) {

        String email = getCurrentUserEmail(principal);
        log.info("Fetching all suggestions request by: {}", email);
        return ResponseEntity.ok(suggestionService.getAllSuggestions());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SuggestionResponseDto>> getUserSuggestions(@PathVariable UUID userId) {
        return ResponseEntity.ok(suggestionService.getUserSuggestions(userId));
    }

    @GetMapping("/{suggestionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuggestionResponseDto> getSuggestionById(
            @PathVariable UUID suggestionId,
            @AuthenticationPrincipal Object principal) {

        String email = getCurrentUserEmail(principal);
        log.info("Fetching suggestions by id request by: {}", email);
        return ResponseEntity.ok(suggestionService.getSuggestionById(suggestionId));
    }
    @GetMapping("/council/{councilId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SuggestionResponseDto>> getSuggestionsByCouncilId(
            @PathVariable UUID councilId,
            @AuthenticationPrincipal Object principal) {

        return ResponseEntity.ok(suggestionService.getSuggestionsByCouncilId(councilId));
    }

    @PutMapping("/{suggestionId}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuggestionResponseDto> approveSuggestion(
            @PathVariable UUID suggestionId,
            @AuthenticationPrincipal Object principal) {

        String email = getCurrentUserEmail(principal);
        UUID approvedById = userService.getCurrentUserId(email);

        return ResponseEntity.ok(suggestionService.approveSuggestion(suggestionId, approvedById));
    }

    @PutMapping("/{suggestionId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuggestionResponseDto> rejectSuggestion(
            @PathVariable UUID suggestionId,
            @RequestParam String rejectionReason,
            @AuthenticationPrincipal Object principal) {

        String email = getCurrentUserEmail(principal);
        UUID rejectedById = userService.getCurrentUserId(email);

        return ResponseEntity.ok(suggestionService.rejectSuggestion(suggestionId, rejectionReason, rejectedById));
    }

    @PutMapping("/{suggestionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuggestionResponseDto> updateSuggestion(
            @PathVariable UUID suggestionId,
            @Valid @RequestBody SuggestionRequestDto dto,
            @AuthenticationPrincipal Object principal) {

        String email = getCurrentUserEmail(principal);
        UUID userId = userService.getCurrentUserId(email);

        return ResponseEntity.ok(suggestionService.updateSuggestion(suggestionId, dto, userId));
    }

    @DeleteMapping("/{suggestionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteSuggestion(
            @PathVariable UUID suggestionId,
            @AuthenticationPrincipal Object principal) {

        String email = getCurrentUserEmail(principal);
        UUID userId = userService.getCurrentUserId(email);

        suggestionService.deleteSuggestion(suggestionId, userId);
        return ResponseEntity.noContent().build();
    }

    private String getCurrentUserEmail(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String email) {
            return email;
        }
        throw new IllegalStateException("Nie znaleziono użytkownika: " + principal.getClass());
    }
}