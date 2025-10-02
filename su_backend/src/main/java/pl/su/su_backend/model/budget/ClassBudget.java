package pl.su.su_backend.model.budget;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.su.su_backend.model.users.Users;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "class_budgets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassBudget {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "class_id", nullable = false)
	private UUID classId;

	@Column
	private Integer year;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false)
	private Users createdBy;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
