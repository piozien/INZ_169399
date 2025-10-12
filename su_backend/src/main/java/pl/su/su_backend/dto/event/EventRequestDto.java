package pl.su.su_backend.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestDto {
	@NotBlank(message = "Event title is required")
	private String title;
	
	@NotBlank(message = "Event description is required")
	private String description;
	
	@NotNull(message = "Start date is required")
	private LocalDateTime startDate;
	
	@NotNull(message = "End date is required")
	private LocalDateTime endDate;
	private String location;
	private String calendarEventId;
}


