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
public class ClassTransactionRequestDto {

	@NotNull
	private UUID budgetId;

	@NotNull
	private TransactionType type;

	@NotNull
	private BigDecimal amount;

	@NotBlank
	private String description;

	@NotNull
	private LocalDateTime date;

	private UUID payerUser; // optional
}
