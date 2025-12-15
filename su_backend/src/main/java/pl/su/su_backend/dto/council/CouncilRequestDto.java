package pl.su.su_backend.dto.council;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilRequestDto {

    @NotBlank(message = "Nazwa samorządu jest wymagana")
    @Size(max = 100, message = "Nazwa nie może być dłuższa niż 100 znaków")
    private String name;

    @NotBlank(message = "Wymagane jest podanie roku szkolnego")
    @Pattern(regexp = "^2\\d{3}\\/2\\d{3}$",
            message = "Nieprawidłowy format roku akademickiego. Oczekiwany format: RRRR/RRRR (np. 2024/2025)")
    private String academicYear;

    @NotNull(message = "Wymagana jest data rozpoczęcia")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "Wymagana jest data zakończenia")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private boolean active;
    private boolean defaultCouncil;
}
