package pl.su.su_backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipDto {
    private UUID councilId;
    private String councilName;
    private String userRole;
    private boolean isActive;
    private LocalDate startDate;
    private LocalDate endDate;
}