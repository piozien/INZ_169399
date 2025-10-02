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
	@NotNull private UUID userId;
	@NotBlank private String title;
	@NotBlank private String description;
	@NotNull private Boolean isAnonymous;
	@NotNull private SuggestionStatus status;
	private Set<String> tags;
}


