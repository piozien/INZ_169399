package pl.su.su_backend.dto.dashboard;

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
public class UserProfileDataDto {
    private UUID id;
    private String email;
    private String fullName;
    private StatusEnum status;
    private List<String> globalRoles;

    private long totalSuggestionsCount;
    private long pendingSuggestionsCount;
    private long approvedSuggestionsCount;

    private List<UserEventDto> userEvents;
    private List<MembershipDto> memberships;
}