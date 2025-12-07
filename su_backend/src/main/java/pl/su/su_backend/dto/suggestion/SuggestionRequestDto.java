package pl.su.su_backend.dto.suggestion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100, message = "Tytuł nie może być dłuższy niż 100 znaków")
	private String title;
	
	@NotBlank(message = "Wymagany jest opis sugestii.")
    @Size(max = 1000, message = "Opis nie może być dłuższy niż 1000 znaków")
	private String description;

	private boolean anonymous;
	private Set<String> tags;
    private UUID councilId;
}


