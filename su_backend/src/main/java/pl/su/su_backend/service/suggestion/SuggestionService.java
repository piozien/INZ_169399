package pl.su.su_backend.service.suggestion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.suggestion.SuggestionRequestDto;
import pl.su.su_backend.dto.suggestion.SuggestionResponseDto;
import pl.su.su_backend.dto.suggestion.SuggestionMapper;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.SuggestionStatus;
import pl.su.su_backend.model.suggestion.Suggestion;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.suggestion.SuggestionRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final UsersRepository usersRepository;
    private final ActivityLogService activityLogService;
    private final PermissionService permissionService;

    public SuggestionResponseDto createSuggestion(SuggestionRequestDto dto, UUID userId) {
        log.info("Creating suggestion by user {}", userId);
        
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));

        Suggestion suggestion = Suggestion.builder()
                .user(user)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .isAnonymous(dto.getIsAnonymous())
                .status(SuggestionStatus.PENDING)
                .build();

        Suggestion savedSuggestion = suggestionRepository.save(suggestion);
        
        activityLogService.log(userId, ActionType.SUGGESTION_CREATE, 
                "Created suggestion: " + dto.getTitle());
        
        log.info("Suggestion created successfully with ID: {}", savedSuggestion.getId());
        return SuggestionMapper.toResponse(savedSuggestion);
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponseDto> getAllSuggestions(String currentUserEmail) {
        log.info("Fetching all suggestions for user: {}", currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.SUGGESTION_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        return suggestionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(suggestion -> SuggestionMapper.toResponse(suggestion, user, permissionService))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<SuggestionResponseDto> getUserSuggestions(UUID userId) {
        log.info("Fetching suggestions for user: {}", userId);
        return suggestionRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(SuggestionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SuggestionResponseDto getSuggestionById(UUID suggestionId, String currentUserEmail) {
        log.info("Fetching suggestion with ID: {} by user: {}", suggestionId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.SUGGESTION_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Suggestion not found"));
        return SuggestionMapper.toResponse(suggestion, user, permissionService);
    }

    public SuggestionResponseDto approveSuggestion(UUID suggestionId, UUID approvedById) {
        log.info("Approving suggestion {} by user {}", suggestionId, approvedById);
        
        if (!permissionService.hasPermission(approvedById, PermissionCode.SUGGESTION_APPROVE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Suggestion not found"));

        if (!SuggestionStatus.PENDING.equals(suggestion.getStatus())) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Only PENDING suggestions can be approved");
        }

        suggestion.setStatus(SuggestionStatus.APPROVED);
        suggestion.setRejectionReason(null); // Clear rejection reason if any
        Suggestion updatedSuggestion = suggestionRepository.save(suggestion);
        
        activityLogService.log(approvedById, ActionType.SUGGESTION_APPROVE, 
                "Approved suggestion: " + suggestion.getTitle());
        
        log.info("Suggestion approved successfully");
        return SuggestionMapper.toResponse(updatedSuggestion);
    }

    public SuggestionResponseDto rejectSuggestion(UUID suggestionId, String rejectionReason, UUID rejectedById) {
        log.info("Rejecting suggestion {} by user {} with reason: {}", suggestionId, rejectedById, rejectionReason);
        
        if (!permissionService.hasPermission(rejectedById, PermissionCode.SUGGESTION_REJECT)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Suggestion not found"));

        if (!SuggestionStatus.PENDING.equals(suggestion.getStatus())) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Only PENDING suggestions can be rejected");
        }

        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Rejection reason is required");
        }

        suggestion.setStatus(SuggestionStatus.REJECTED);
        suggestion.setRejectionReason(rejectionReason.trim());
        Suggestion updatedSuggestion = suggestionRepository.save(suggestion);
        
        activityLogService.log(rejectedById, ActionType.SUGGESTION_REJECT, 
                "Rejected suggestion: " + suggestion.getTitle() + " - Reason: " + rejectionReason);
        
        log.info("Suggestion rejected successfully");
        return SuggestionMapper.toResponse(updatedSuggestion);
    }

    public SuggestionResponseDto updateSuggestion(UUID suggestionId, SuggestionRequestDto dto, UUID userId) {
        log.info("Updating suggestion {} by user {}", suggestionId, userId);
        
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Suggestion not found"));

        boolean isOwner = suggestion.getUser().getId().equals(userId);
        boolean canEdit = permissionService.hasPermission(userId, PermissionCode.SUGGESTION_EDIT);
        if (!isOwner && !canEdit) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        if (!SuggestionStatus.PENDING.equals(suggestion.getStatus())) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Cannot edit non-pending suggestion");
        }

        suggestion.setTitle(dto.getTitle());
        suggestion.setDescription(dto.getDescription());
        suggestion.setIsAnonymous(dto.getIsAnonymous());

        Suggestion updatedSuggestion = suggestionRepository.save(suggestion);
        
        activityLogService.log(userId, ActionType.SUGGESTION_UPDATE, 
                "Updated suggestion: " + dto.getTitle());
        
        log.info("Suggestion updated successfully");
        return SuggestionMapper.toResponse(updatedSuggestion);
    }

    public void deleteSuggestion(UUID suggestionId, UUID userId) {
        log.info("Deleting suggestion {} by user {}", suggestionId, userId);
        
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Suggestion not found"));

        boolean isOwner = suggestion.getUser().getId().equals(userId);
        boolean canDelete = permissionService.hasPermission(userId, PermissionCode.SUGGESTION_DELETE);
        
        if (!isOwner && !canDelete) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        suggestionRepository.delete(suggestion);
        
        activityLogService.log(userId, ActionType.SUGGESTION_DELETE, 
                "Deleted suggestion: " + suggestion.getTitle());
        
        log.info("Suggestion deleted successfully");
    }
}
