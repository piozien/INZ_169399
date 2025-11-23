package pl.su.su_backend.dto.council;

import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.service.council.CouncilMemberService;

import java.util.List;
import java.util.stream.Collectors;

public class CouncilMapper {

    private CouncilMapper() {
    }

    public static CouncilResponseDto toResponseDto(Council council, CouncilMemberService councilMemberService) {
        if (council == null) {
            return null;
        }

        List<CouncilMember> councilMembers = councilMemberService.getCouncilMembersInternal(council.getId());
        List<CouncilMemberDto> memberDto = councilMembers.stream()
                .map(CouncilMemberMapper::toDto)
                .collect(Collectors.toList());

        return CouncilResponseDto.builder()
                .id(council.getId())
                .name(council.getName())
                .academicYear(council.getAcademicYear())
                .startDate(council.getStartDate())
                .endDate(council.getEndDate())
                .isActive(council.getIsActive())
                .joinCode(council.getJoinCode())
                .createdAt(council.getCreatedAt())
                .members(memberDto)
                .build();
    }

    public static Council toEntity(CouncilRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return Council.builder()
                .name(dto.getName())
                .academicYear(dto.getAcademicYear())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isActive(true)
                .build();
    }

}
