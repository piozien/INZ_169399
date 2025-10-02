package pl.su.su_backend.dto.role;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.model.enums.RoleCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDto {

    @NotNull(message = "Role code is required")
    private RoleCode roleCode;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}

