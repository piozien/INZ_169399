package pl.su.su_backend.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.model.enums.EventStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDto {
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private Integer maxParticipants;
    private Integer participantsCount;
    private UUID createdById;
    private UUID councilId;
    private LocalDateTime createdAt;
    private EventStatus status;
    private List<ParticipantResponseDto> participants;
}