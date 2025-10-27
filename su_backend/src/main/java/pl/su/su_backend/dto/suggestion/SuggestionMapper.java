package pl.su.su_backend.dto.suggestion;

import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.suggestion.Suggestion;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.service.auth.PermissionService;

import java.util.UUID;
import java.util.stream.Collectors;

public class SuggestionMapper {

	public static SuggestionResponseDto toResponse(Suggestion s) {
		if (s == null) return null;
		return SuggestionResponseDto.builder()
				.id(s.getId())
				.userId(s.getUser() != null ? s.getUser().getId() : null)
				.title(s.getTitle())
				.description(s.getDescription())
				.isAnonymous(s.getIsAnonymous())
				.status(s.getStatus())
				.rejectionReason(s.getRejectionReason())
				.createdAt(s.getCreatedAt())
				.tags(s.getTags() == null ? null : s.getTags().stream().map(t -> t.getId().getTag()).collect(Collectors.toSet()))
				.build();
	}

	public static SuggestionResponseDto toResponse(Suggestion s, Users currentUser, PermissionService permissionService) {
		if (s == null) return null;

		UUID userId = null;
		if (s.getUser() != null) {
			boolean isAnonymous = s.getIsAnonymous() != null && s.getIsAnonymous();
			boolean canViewAnonymous = currentUser != null && 
				permissionService.hasPermission(currentUser.getId(), PermissionCode.SUGGESTION_VIEW_ANONYMOUS);
			
			if (!isAnonymous || canViewAnonymous) {
				userId = s.getUser().getId();
			}
		}
		
		return SuggestionResponseDto.builder()
				.id(s.getId())
				.userId(userId)
				.title(s.getTitle())
				.description(s.getDescription())
				.isAnonymous(s.getIsAnonymous())
				.status(s.getStatus())
				.rejectionReason(s.getRejectionReason())
				.createdAt(s.getCreatedAt())
				.tags(s.getTags() == null ? null : s.getTags().stream().map(t -> t.getId().getTag()).collect(Collectors.toSet()))
				.build();
	}
}


