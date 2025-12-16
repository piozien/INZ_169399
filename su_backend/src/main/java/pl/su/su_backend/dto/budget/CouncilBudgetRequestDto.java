package pl.su.su_backend.dto.budget;

import jakarta.validation.constraints.NotBlank;
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

    @NotNull(message = "Należy wskazać do jakiego samorządu przypisać budżet")
	private UUID councilId;

    @NotBlank(message = "Rok nie może być pusty")
	private String year;

	@NotNull(message = "Wymagana jest kwota początkowa")
	private BigDecimal initialAmount;
}
