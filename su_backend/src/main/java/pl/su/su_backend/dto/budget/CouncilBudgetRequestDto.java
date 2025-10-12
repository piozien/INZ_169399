package pl.su.su_backend.dto.budget;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilBudgetRequestDto {

	@NotNull(message = "Council ID is required")
	private UUID councilId;

	@NotNull(message = "Year is required")
	private Integer year;

	@NotNull(message = "Initial amount is required")
	private BigDecimal initialAmount;
}
