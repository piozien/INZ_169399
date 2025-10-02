package pl.su.su_backend.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.model.enums.EventParticipantRole;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantRequestDto {
	@NotNull private UUID eventId;
	@NotNull private UUID userId;
	@NotNull private EventParticipantRole role;
}


