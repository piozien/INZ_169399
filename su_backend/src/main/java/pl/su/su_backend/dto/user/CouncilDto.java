package pl.su.su_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.dto.council.CouncilMemberDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilDto {
    private UUID id;
    private String name;
    private String invitationCode;
    private LocalDate createdAt;
    private List<CouncilMemberDto> members;
}
