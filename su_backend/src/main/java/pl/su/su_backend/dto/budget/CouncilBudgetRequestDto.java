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
public class CouncilBudgetRequestDto {

	@NotNull
	private UUID councilId;

	private Integer year;
}
