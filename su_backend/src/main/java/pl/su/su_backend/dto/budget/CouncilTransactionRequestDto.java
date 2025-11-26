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

	@NotNull(message = "Wymagany jest identyfikator budżetu")
	private UUID budgetId;

	@NotNull(message = "Wymagany jest typ transakcji")
	private TransactionType type;

	@NotNull(message = "Kwota jest wymagana")
	private BigDecimal amount;

	@NotBlank(message = "Wymagany jest opis")
	private String description;

	@NotNull(message = "Data jest wymagana")
	private LocalDateTime date;
}
