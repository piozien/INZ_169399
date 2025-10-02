package pl.su.su_backend.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
	private UUID createdById;
	private String calendarEventId;
	private LocalDateTime createdAt;
}


