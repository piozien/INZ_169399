package pl.su.su_backend.dto.council;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilResponseDto {

    private UUID id;
    private String name;
    private String academicYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private boolean defaultCouncil;

    private String joinCode;
    private LocalDateTime createdAt;
    private List<CouncilMemberDto> members;

    private Set<String> myPermissions;
}
