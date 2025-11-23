package pl.su.su_backend.dto.user;

import pl.su.su_backend.model.users.Users;


public class UserMapper {

    public static UserResponseDto toResponseDto(Users user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .authProvider(user.getAuthProvider())
                .externalId(user.getExternalId())
                .build();
    }
}

