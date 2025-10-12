package pl.su.su_backend.dto.classes;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassesRequestDto {

    @NotBlank(message = "Class name is required")
    private String name;

    @NotBlank(message = "Year is required")
    private String year;
}


