package pl.su.su_backend.dto.role;

import pl.su.su_backend.model.roles.Role;

public class RoleMapper {

    private RoleMapper() {
        // Utility class - private constructor
    }

    /**
     * Converts Role entity to RoleResponseDto
     */
    public static RoleResponseDto toResponseDto(Role role) {
        if (role == null) {
            return null;
        }

        return RoleResponseDto.builder()
                .id(role.getId())
                .roleCode(role.getRoleCode())
                .displayName(role.getDisplayName())
                .category(role.getCategory())
                .description(role.getDescription())
                .build();
    }

    /**
     * Converts RoleRequestDto to Role entity
     * Note: Does not set id (managed by the database)
     */
    public static Role toEntity(RoleRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return Role.builder()
                .roleCode(dto.getRoleCode())
                .description(dto.getDescription())
                .build();
    }

    /**
     * Updates existing Role entity with data from RoleRequestDto
     * Note: Does not update id
     */
    public static void updateEntity(Role role, RoleRequestDto dto) {
        if (role == null || dto == null) {
            return;
        }

        role.setRoleCode(dto.getRoleCode());
        role.setDescription(dto.getDescription());
    }
}

