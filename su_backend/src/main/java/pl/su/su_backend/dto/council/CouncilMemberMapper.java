package pl.su.su_backend.dto.council;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.su.su_backend.model.council.CouncilMember;

@Mapper(componentModel = "spring")
public interface CouncilMemberMapper {

    @Mapping(target = "councilId", source = "id.councilId")
    @Mapping(target = "userId", source = "id.userId")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "roleName", expression = "java(councilMember.getRole().getDisplayName())")
    CouncilMemberDto toDto(CouncilMember councilMember);
}