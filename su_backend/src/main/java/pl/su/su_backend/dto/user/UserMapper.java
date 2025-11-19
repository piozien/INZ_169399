package pl.su.su_backend.dto.user;

import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.model.permissions.Permission;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserResponseDto toResponseDto(Users user, Classes studentClass, Council council) {
        if (user == null) {
            return null;
        }

        ClassDto classDto = null;
        if (studentClass != null) {
            classDto = ClassDto.builder()
                    .id(studentClass.getId())
                    .name(studentClass.getName())
                    .build();
        }

        CouncilDto councilDto = null;
        if (council != null) {
            councilDto = CouncilDto.builder()
                    .id(council.getId())
                    .name(council.getName())
                    .build();
        }

        List<String> roles = user.getUserRoles() != null ?
                user.getUserRoles().stream()
                        .map(UserRole::getRole)
                        .sorted(Comparator.comparingInt((Role role) -> role.getRoleCode().getRank()).reversed())
                        .map(role -> role.getRoleCode().name())
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        Set<String> permissions = user.getUserRoles() != null ?
                user.getUserRoles().stream()
                        .flatMap(userRole -> userRole.getRole().getPermissions().stream())
                        .map(Permission::getName)
                        .collect(Collectors.toSet()) :
                Collections.emptySet();

        return UserResponseDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .authProvider(user.getAuthProvider())
                .externalId(user.getExternalId())
                .studentClass(classDto)
                .council(councilDto)
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    public static UserResponseDto toResponseDto(Users user) {
        return toResponseDto(user, user.getClasses(), null);
    }
}

