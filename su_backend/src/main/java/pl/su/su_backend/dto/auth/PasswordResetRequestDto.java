package pl.su.su_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDto {

    @NotBlank(message = "Wymagany jest adres e-mail")
    @Email(message = "Adres e-mail powinien być prawidłowy.")
    private String email;
}
