package pl.su.su_backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.StatusEnum;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {

    @NotBlank(message = "Wymagane jest podanie pełnego imienia i nazwiska.")
    @Size(min = 2, max = 100, message = "Pełna nazwa musi zawierać od 2 do 100 znaków.")
    private String fullName;

    private StatusEnum status;

    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    private String externalId;
}

