package pl.su.su_backend.dto.council;

import pl.su.su_backend.dto.user.UserMapper;
import pl.su.su_backend.model.council.Council;

import java.util.List;
import java.util.stream.Collectors;

public class CouncilMapper {

    private CouncilMapper() {
    }

    public static CouncilResponseDto toResponseDto(Council council) {
        if (council == null) {
            return null;
        }

        return CouncilResponseDto.builder()
                .id(council.getId())
                .name(council.getName())
                .academicYear(council.getAcademicYear())
                .startDate(council.getStartDate())
                .endDate(council.getEndDate())
                .isActive(council.getIsActive())
                .joinCode(council.getJoinCode())
                .createdAt(council.getCreatedAt())
                .members(council.getMembers() != null ? 
                    council.getMembers().stream()
                        .map(UserMapper::toResponseDto)
                        .collect(Collectors.toList()) : 
                    List.of())
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
