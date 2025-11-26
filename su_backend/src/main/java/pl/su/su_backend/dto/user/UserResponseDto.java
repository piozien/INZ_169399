package pl.su.su_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.model.enums.StatusEnum;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String email;
    private String fullName;
    private StatusEnum status;

    private List<String> roles;
}