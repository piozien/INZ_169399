package pl.su.su_backend.dto.budget;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassBudgetRequestDto {

	@NotNull
	private UUID classId;

	private Integer year;
}
