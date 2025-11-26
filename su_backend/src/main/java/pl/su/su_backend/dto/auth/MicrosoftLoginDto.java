package pl.su.su_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MicrosoftLoginDto {

    @NotBlank(message = "Wymagany jest token")
    private String token;
}