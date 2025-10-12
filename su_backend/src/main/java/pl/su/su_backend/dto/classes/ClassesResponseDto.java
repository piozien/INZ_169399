package pl.su.su_backend.dto.classes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassesResponseDto {
    private UUID id;
    private String name;
    private String year;
}


