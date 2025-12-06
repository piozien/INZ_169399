package pl.su.su_backend.dto.suggestion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionRequestDto {
	@NotNull(message = "Wymagany jest identyfikator użytkownika")
	private UUID userId;
	
	@NotBlank(message = "Tytuł sugestii jest wymagany")
	private String title;
	
	@NotBlank(message = "Wymagany jest opis sugestii.")
	private String description;
	
	@NotNull()
	private Boolean isAnonymous;

	private Set<String> tags;

    private UUID councilId;
}


