package pl.su.su_backend.dto.suggestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.model.enums.SuggestionStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionResponseDto {
	private UUID id;
	private UUID userId;
    private UUID councilId;
	private String title;
	private String description;
	private Boolean isAnonymous;
	private SuggestionStatus status;
	private String rejectionReason;
	private LocalDateTime createdAt;
	private Set<String> tags;
}


