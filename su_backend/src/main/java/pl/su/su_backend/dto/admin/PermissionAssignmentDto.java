package pl.su.su_backend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionAssignmentDto {
    private RoleCode roleCode;
    private PermissionCode permission;
}
