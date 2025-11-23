package pl.su.su_backend.dto.council;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.model.enums.RoleCode;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilMemberDto {
    private UUID councilId;
    private UUID userId;
    private String userFullName;
    private String userEmail;
    private RoleCode role;
    private String roleName;
}
