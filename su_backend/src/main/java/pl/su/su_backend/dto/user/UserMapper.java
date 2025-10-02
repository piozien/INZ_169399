package pl.su.su_backend.dto.user;

import pl.su.su_backend.model.users.Users;

public class UserMapper {

    private UserMapper() {
        // Utility class - private constructor
    }

    public static UserResponseDto toResponseDto(Users user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .status(user.getStatus())
                .classId(user.getClassId())
                .createdAt(user.getCreatedAt())
                .authProvider(user.getAuthProvider())
                .externalId(user.getExternalId())
                .build();
    }

    public static Users toEntity(UserRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return Users.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .status(dto.getStatus())
                .classId(dto.getClassId())
                .authProvider(dto.getAuthProvider())
                .externalId(dto.getExternalId())
                .build();
    }

    public static void updateEntity(Users user, UserRequestDto dto) {
        if (user == null || dto == null) {
            return;
        }

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        user.setClassId(dto.getClassId());
        user.setAuthProvider(dto.getAuthProvider());
        user.setExternalId(dto.getExternalId());
    }
}

