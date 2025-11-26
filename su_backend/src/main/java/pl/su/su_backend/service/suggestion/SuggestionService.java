package pl.su.su_backend.service.suggestion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.suggestion.SuggestionMapper;
import pl.su.su_backend.dto.suggestion.SuggestionRequestDto;
import pl.su.su_backend.dto.suggestion.SuggestionResponseDto;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.SuggestionStatus;
import pl.su.su_backend.model.suggestion.Suggestion;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.suggestion.SuggestionRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.service.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final UserService userService;
    private final UsersRepository usersRepository;
    private final ActivityLogService activityLogService;
    private final PermissionService permissionService;
    private final SuggestionMapper suggestionMapper;

    public SuggestionResponseDto createSuggestion(SuggestionRequestDto dto, UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono użytkownika"));
        Users userEntity = userService.getUserByEmailEntity(user.getEmail());

        Suggestion suggestion = suggestionMapper.toEntity(dto);
        suggestion.setUser(userEntity);
        suggestion.setCreatedAt(LocalDateTime.now());
        suggestion.setStatus(SuggestionStatus.PENDING);

        Suggestion savedSuggestion = suggestionRepository.save(suggestion);

        activityLogService.log(userId, ActionType.SUGGESTION_CREATE,
                "Utworzono sugestię: " + dto.getTitle());

        return suggestionMapper.toResponse(savedSuggestion);
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponseDto> getAllSuggestions(String currentUserEmail) {
        if (!permissionService.hasPermission(currentUserEmail, PermissionCode.SUGGESTION_VIEW)) {
            throw ApiException.forbidden("Brak dostępu");
        }

        return suggestionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(suggestionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponseDto> getUserSuggestions(UUID userId) {
        return suggestionRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(suggestionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SuggestionResponseDto getSuggestionById(UUID suggestionId, String currentUserEmail) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono sugestii"));

        Users currentUser = userService.getUserByEmailEntity(currentUserEmail);

        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.SUGGESTION_VIEW)) {
            throw ApiException.forbidden("Brak dostępu do podglądu sugestii");
        }

        SuggestionResponseDto dto = suggestionMapper.toResponse(suggestion);

        if (Boolean.TRUE.equals(suggestion.getIsAnonymous())) {
            boolean isAuthor = suggestion.getUser().getId().equals(currentUser.getId());
            boolean canViewAnonymous = permissionService.hasPermission(currentUser.getId(), PermissionCode.SUGGESTION_VIEW_ANONYMOUS);

            if (!isAuthor && !canViewAnonymous) {
                dto.setUserId(null);
            }
        }

        return dto;
    }

    public SuggestionResponseDto approveSuggestion(UUID suggestionId, UUID approvedById) {
        if (!permissionService.hasPermission(approvedById, PermissionCode.SUGGESTION_APPROVE)) {
            throw ApiException.forbidden("Brak uprawnień do zatwierdzania sugestii.");
        }

        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono sugestii"));

        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw ApiException.badRequest("Można zatwierdzać tylko oczekujące sugestie");
        }

        suggestion.setStatus(SuggestionStatus.APPROVED);
        suggestion.setRejectionReason(null);

        Suggestion saved = suggestionRepository.save(suggestion);
        activityLogService.log(approvedById, ActionType.SUGGESTION_APPROVE, "Zatwierdzono: " + suggestion.getTitle());

        return suggestionMapper.toResponse(saved);
    }

    public SuggestionResponseDto rejectSuggestion(UUID suggestionId, String reason, UUID rejectedById) {
        if (!permissionService.hasPermission(rejectedById, PermissionCode.SUGGESTION_APPROVE)) {
            throw ApiException.forbidden("Brak uprawnień do odrzucania");
        }

        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono sugestii"));

        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw ApiException.badRequest("Można odrzucać tylko oczekujące sugestie");
        }

        suggestion.setStatus(SuggestionStatus.REJECTED);
        suggestion.setRejectionReason(reason);

        Suggestion saved = suggestionRepository.save(suggestion);
        activityLogService.log(rejectedById, ActionType.SUGGESTION_REJECT, "Odrzucono: " + suggestion.getTitle());

        return suggestionMapper.toResponse(saved);
    }

    public SuggestionResponseDto updateSuggestion(UUID suggestionId, SuggestionRequestDto dto, UUID userId) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono sugestii"));

        boolean isOwner = suggestion.getUser().getId().equals(userId);
        boolean canEdit = permissionService.hasPermission(userId, PermissionCode.SUGGESTION_EDIT);

        if (!isOwner && !canEdit) {
            throw ApiException.forbidden("Brak uprawnień do edycji");
        }

        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw ApiException.badRequest("Można edytować tylko oczekujące sugestie");
        }

        suggestion.setTitle(dto.getTitle());
        suggestion.setDescription(dto.getDescription());
        suggestion.setIsAnonymous(dto.getIsAnonymous());

        Suggestion saved = suggestionRepository.save(suggestion);
        activityLogService.log(userId, ActionType.SUGGESTION_UPDATE, "Zaktualizowano: " + dto.getTitle());

        return suggestionMapper.toResponse(saved);
    }

    public void deleteSuggestion(UUID suggestionId, UUID userId) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono sugestii"));

        boolean isOwner = suggestion.getUser().getId().equals(userId);
        boolean canDelete = permissionService.hasPermission(userId, PermissionCode.SUGGESTION_DELETE);

        if (!isOwner && !canDelete) {
            throw ApiException.forbidden("Brak uprawnień do usunięcia");
        }

        suggestionRepository.delete(suggestion);
        activityLogService.log(userId, ActionType.SUGGESTION_DELETE, "Usunięto sugestię: " + suggestion.getTitle());
    }
}