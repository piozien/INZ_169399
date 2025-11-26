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
    @NotBlank(message = "Tytuł wydarzenia jest wymagany")
    private String title;

    @NotBlank(message = "Opis jest wymagany")
    private String description;

    @NotNull(message = "Data rozpoczęcia jest wymagana")
    private LocalDateTime startDate;

    @NotNull(message = "Data zakończenia jest wymagana")
    private LocalDateTime endDate;

    private String location;

    private String calendarEventId;
}