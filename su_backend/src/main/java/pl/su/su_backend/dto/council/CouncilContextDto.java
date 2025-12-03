package pl.su.su_backend.dto.council;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class CouncilContextDto {
    private boolean isMember;
    private String role;
    private Set<String> permissions;

}