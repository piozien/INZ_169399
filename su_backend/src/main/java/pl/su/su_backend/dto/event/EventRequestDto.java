package pl.su.su_backend.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class EventRequestDto {
    @NotBlank(message = "Tytuł wydarzenia jest wymagany")
    @Size(max = 100, message = "Tytuł nie może być dłuższy niż 100 znaków")
    private String title;

    @NotBlank(message = "Opis jest wymagany")
    @Size(max = 1000, message = "Opis nie może być dłuższy niż 1000 znaków")
    private String description;

    @NotNull(message = "Data rozpoczęcia jest wymagana")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @NotNull(message = "Data zakończenia jest wymagana")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    private String location;

    private String calendarEventId;

    private UUID councilId;
}