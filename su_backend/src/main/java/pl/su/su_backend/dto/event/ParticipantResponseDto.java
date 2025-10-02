package pl.su.su_backend.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.model.enums.EventParticipantRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponseDto {
	private UUID eventId;
	private UUID userId;
	private EventParticipantRole role;
	private Boolean confirmed;
	private LocalDateTime assignedAt;
}


