package pl.su.su_backend.dto.council;

import pl.su.su_backend.model.council.CouncilMember;

public class CouncilMemberMapper {

    public static CouncilMemberDto toDto(CouncilMember councilMember) {
        if (councilMember == null) {
            return null;
        }

        return CouncilMemberDto.builder()
                .councilId(councilMember.getId().getCouncilId())
                .userId(councilMember.getId().getUserId())
                .userFullName(councilMember.getUser().getFullName())
                .userEmail(councilMember.getUser().getEmail())
                .role(councilMember.getRole())
                .roleName(councilMember.getRole().getDisplayName())
                .build();
    }
}

