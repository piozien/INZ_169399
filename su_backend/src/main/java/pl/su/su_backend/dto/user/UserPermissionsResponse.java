package pl.su.su_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPermissionsResponse {
    private Set<String> roles;
    private Set<String> permissions;
}
