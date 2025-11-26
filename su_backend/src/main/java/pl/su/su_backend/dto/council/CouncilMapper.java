package pl.su.su_backend.dto.council;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.su.su_backend.model.council.Council;

@Mapper(componentModel = "spring", uses = CouncilMemberMapper.class)
public interface CouncilMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "joinCode", ignore = true)
    @Mapping(target = "members", ignore = true)
    Council toEntity(CouncilRequestDto dto);

    @Mapping(target = "members", source = "members")
    CouncilResponseDto toResponseDto(Council council);
}