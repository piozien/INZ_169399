package pl.su.su_backend.dto.suggestion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.model.enums.SuggestionStatus;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionRequestDto {
	@NotNull(message = "User ID is required")
	private UUID userId;
	
	@NotBlank(message = "Suggestion title is required")
	private String title;
	
	@NotBlank(message = "Suggestion description is required")
	private String description;
	
	@NotNull(message = "Anonymous flag is required")
	private Boolean isAnonymous;
	
	@NotNull(message = "Suggestion status is required")
	private SuggestionStatus status;
	private Set<String> tags;
}


