package pl.su.su_backend.dto.suggestion;

import pl.su.su_backend.model.suggestion.Suggestion;

import java.util.stream.Collectors;

public class SuggestionMapper {

	private SuggestionMapper() {}

	public static SuggestionResponseDto toResponse(Suggestion s) {
		if (s == null) return null;
		return SuggestionResponseDto.builder()
				.id(s.getId())
				.userId(s.getUser() != null ? s.getUser().getId() : null)
				.title(s.getTitle())
				.description(s.getDescription())
				.isAnonymous(s.getIsAnonymous())
				.status(s.getStatus())
				.createdAt(s.getCreatedAt())
				.tags(s.getTags() == null ? null : s.getTags().stream().map(t -> t.getId().getTag()).collect(Collectors.toSet()))
				.build();
	}
}


