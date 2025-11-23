package pl.su.su_backend.model.budget;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.enums.TransactionType;
import pl.su.su_backend.model.users.Users;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "council_budgets",
	uniqueConstraints = @UniqueConstraint(columnNames = {"council_id", "year"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilBudget {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "council_id", nullable = false)
	private Council council;

	@Column
	private String year;

	@Column(name = "initial_amount", precision = 12, scale = 2)
	private BigDecimal initialAmount;

	@Column(name = "balance", precision = 12, scale = 2, nullable = false)
	private BigDecimal balance;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false)
	private Users createdBy;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<CouncilTransaction> transactions = new ArrayList<>();

	@PrePersist
	public void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		if (balance == null) {
			balance = initialAmount != null ? initialAmount : BigDecimal.ZERO;
		}
	}
}
