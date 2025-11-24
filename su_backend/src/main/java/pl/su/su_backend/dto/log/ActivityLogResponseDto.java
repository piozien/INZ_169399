package pl.su.su_backend.dto.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.model.enums.ActionType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponseDto {
	private UUID id;
	private UUID userId;
	private ActionType actionType;
	private String action;
	private LocalDateTime createdAt;
}


