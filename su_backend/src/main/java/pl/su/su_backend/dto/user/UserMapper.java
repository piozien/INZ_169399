package pl.su.su_backend.dto.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "userRoles", qualifiedByName = "mapRolesToStrings")
    UserResponseDto toResponseDto(Users user);

    @Named("mapRolesToStrings")
    default List<String> mapRolesToStrings(Set<UserRole> userRoles) {
        if (userRoles == null) {
            return List.of();
        }
        return userRoles.stream()
                .map(ur -> ur.getRole().getRoleCode().name())
                .collect(Collectors.toList());
    }
}