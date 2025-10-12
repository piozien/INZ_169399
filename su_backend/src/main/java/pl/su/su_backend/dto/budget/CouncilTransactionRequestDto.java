package pl.su.su_backend.dto.budget;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.su.su_backend.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilTransactionRequestDto {

	@NotNull(message = "Budget ID is required")
	private UUID budgetId;

	@NotNull(message = "Transaction type is required")
	private TransactionType type;

	@NotNull(message = "Amount is required")
	private BigDecimal amount;

	@NotBlank(message = "Description is required")
	private String description;

	@NotNull(message = "Date is required")
	private LocalDateTime date;
}
