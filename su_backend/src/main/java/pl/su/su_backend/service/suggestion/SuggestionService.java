package pl.su.su_backend.service.suggestion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.suggestion.SuggestionMapper;
import pl.su.su_backend.dto.suggestion.SuggestionRequestDto;
import pl.su.su_backend.dto.suggestion.SuggestionResponseDto;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.SuggestionStatus;
import pl.su.su_backend.model.suggestion.Suggestion;
import pl.su.su_backend.model.suggestion.SuggestionTag;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.repositories.suggestion.SuggestionRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final UsersRepository usersRepository;
    private final CouncilRepository councilRepository;
    private final PermissionService permissionService;
    private final SuggestionMapper suggestionMapper;

    public SuggestionResponseDto createSuggestion(SuggestionRequestDto dto, UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono użytkownika"));

        Council council;

        if (dto.getCouncilId() != null) {
            council = councilRepository.findById(dto.getCouncilId())
                    .orElseThrow(() -> ApiException.notFound("Nie znaleziono wskazanego samorządu"));
        } else {
            council = councilRepository.findFirstByActiveTrueAndDefaultCouncilTrue()
                    .orElseThrow(() -> ApiException.badRequest(
                            "W systemie nie ma ustawionego domyślnego samorządu."
                    ));
        }

        Suggestion suggestion = suggestionMapper.toEntity(dto);
        suggestion.setUser(user);
        suggestion.setCouncil(council);
        suggestion.setCreatedAt(LocalDateTime.now());

        if (suggestion.getStatus() == null) {
            suggestion.setStatus(SuggestionStatus.PENDING);
        }

        suggestion.setAnonymous(dto.isAnonymous());

        Suggestion savedSuggestion = suggestionRepository.save(suggestion);

        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            for (String tagString : dto.getTags()) {
                SuggestionTag newTag = new SuggestionTag();
                SuggestionTag.Id tagId = new SuggestionTag.Id(savedSuggestion.getId(), tagString);
                newTag.setId(tagId);
                newTag.setSuggestion(savedSuggestion);

                savedSuggestion.getTags().add(newTag);
            }
            savedSuggestion = suggestionRepository.save(savedSuggestion);
        }

        return suggestionMapper.toResponse(savedSuggestion);
    }

    public SuggestionResponseDto approveSuggestion(UUID suggestionId, UUID approvedById) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.notFound("Sugestia nie istnieje"));

        if (!permissionService.hasPermission(approvedById, PermissionCode.SUGGESTION_APPROVE, suggestion.getCouncil().getId())) {
            throw ApiException.forbidden("Brak uprawnień do zatwierdzania sugestii w tym samorządzie.");
        }

        suggestion.setStatus(SuggestionStatus.APPROVED);
        return suggestionMapper.toResponse(suggestionRepository.save(suggestion));
    }

    public SuggestionResponseDto rejectSuggestion(UUID suggestionId, String reason, UUID rejectedById) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.notFound("Sugestia nie istnieje"));

        if (!permissionService.hasPermission(rejectedById, PermissionCode.SUGGESTION_DELETE, suggestion.getCouncil().getId())) {
            throw ApiException.forbidden("Brak uprawnień do odrzucania sugestii.");
        }

        suggestion.setStatus(SuggestionStatus.REJECTED);
        suggestion.setRejectionReason(reason);
        return suggestionMapper.toResponse(suggestionRepository.save(suggestion));
    }

    public SuggestionResponseDto updateSuggestion(UUID suggestionId, SuggestionRequestDto dto, UUID userId) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.notFound("Sugestia nie istnieje"));

        boolean isAuthor = suggestion.getUser().getId().equals(userId);
        boolean isAdmin = permissionService.hasPermission(userId, PermissionCode.SUGGESTION_EDIT, suggestion.getCouncil().getId());

        if (!isAuthor && !isAdmin) {
            throw ApiException.forbidden("Brak uprawnień do edycji");
        }

        suggestion.setTitle(dto.getTitle());
        suggestion.setDescription(dto.getDescription());
        suggestion.setAnonymous(dto.isAnonymous());

        if (dto.getTags() != null) {
            suggestion.getTags().clear();

            for (String tagString : dto.getTags()) {
                SuggestionTag newTag = new SuggestionTag();
                SuggestionTag.Id tagId = new SuggestionTag.Id(suggestion.getId(), tagString);
                newTag.setId(tagId);
                newTag.setSuggestion(suggestion);

                suggestion.getTags().add(newTag);
            }
        }

        return suggestionMapper.toResponse(suggestionRepository.save(suggestion));
    }

    public void deleteSuggestion(UUID suggestionId, UUID userId) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.notFound("Sugestia nie istnieje"));

        boolean isAuthor = suggestion.getUser().getId().equals(userId);
        boolean isAdmin = permissionService.hasPermission(userId, PermissionCode.SUGGESTION_DELETE, suggestion.getCouncil().getId());

        if (!isAuthor && !isAdmin) {
            throw ApiException.forbidden("Brak uprawnień do usunięcia");
        }
        suggestionRepository.delete(suggestion);
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponseDto> getAllSuggestions() {
        return suggestionRepository.findAll().stream()
                .map(suggestionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponseDto> getSuggestionsByCouncilId(UUID councilId) {
        return suggestionRepository.findByCouncil_IdOrderByCreatedAtDesc(councilId).stream()
                .map(suggestionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponseDto> getUserSuggestions(UUID userId) {
        return suggestionRepository.findByUser_Id(userId).stream()
                .map(suggestionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SuggestionResponseDto getSuggestionById(UUID suggestionId) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.notFound("Sugestia nie istnieje"));
        return suggestionMapper.toResponse(suggestion);
    }
}