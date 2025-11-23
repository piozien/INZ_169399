package pl.su.su_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.StatusEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private UUID id;
    private String fullName;
    private String email;
    private StatusEnum status;
    private LocalDateTime createdAt;
    private AuthProvider authProvider;
    private String externalId;

    private ClassDto studentClass;
    private List<CouncilDto> councils;
    private List<String> roles;
    private Set<String> permissions;
}

