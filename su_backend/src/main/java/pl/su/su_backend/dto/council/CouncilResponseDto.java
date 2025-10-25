package pl.su.su_backend.dto.council;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.dto.user.UserResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
    private Boolean isActive;
    private String joinCode;
    private LocalDateTime createdAt;
    private List<UserResponseDto> members;
}
