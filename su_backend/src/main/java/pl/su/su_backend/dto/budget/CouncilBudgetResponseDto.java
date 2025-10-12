package pl.su.su_backend.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilBudgetResponseDto {

	private UUID id;
	private UUID councilId;
	private Integer year;
	private BigDecimal initialAmount;
	private UUID createdById;
	private LocalDateTime createdAt;
	private BigDecimal totalIncome;
	private BigDecimal totalExpenses;
	private BigDecimal balance;
}
