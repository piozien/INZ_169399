package pl.su.su_backend.dto.council;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String name;

    @NotBlank(message = "Wymagane jest podanie roku szkolnego")
    private String academicYear;

    @NotNull(message = "Wymagana jest data rozpoczęcia")
    private LocalDate startDate;

    @NotNull(message = "Wymagana jest data zakończenia")
    private LocalDate endDate;

    private boolean active;
    private boolean defaultCouncil;
}
