package pl.su.su_backend.dto.classes;

import pl.su.su_backend.model.classes.Classes;

public class ClassesMapper {

    private ClassesMapper() {}

    public static ClassesResponseDto toResponse(Classes c) {
        if (c == null) return null;
        return ClassesResponseDto.builder()
                .id(c.getId())
                .name(c.getName())
                .year(c.getYear())
                .build();
    }

    public static Classes toEntity(ClassesRequestDto dto) {
        if (dto == null) return null;
        return Classes.builder()
                .name(dto.getName())
                .year(dto.getYear())
                .build();
    }
}


