package pl.su.su_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetConfirmDto {

    @NotBlank(message = "Wymagany jest token")
    private String token;

    @NotBlank(message = "Wymagane hasło")
    @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków.")
    private String newPassword;
}
