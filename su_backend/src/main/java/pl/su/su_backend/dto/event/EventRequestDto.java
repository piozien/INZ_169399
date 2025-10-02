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
	@NotBlank private String title;
	@NotBlank private String description;
	@NotNull private LocalDateTime startDate;
	@NotNull private LocalDateTime endDate;
	private String location;
	private String calendarEventId;
}


