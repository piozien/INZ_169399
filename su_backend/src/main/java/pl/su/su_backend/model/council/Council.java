package pl.su.su_backend.model.council;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.su.su_backend.model.users.Users;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "councils",
	uniqueConstraints = @UniqueConstraint(columnNames = {"name", "academic_year"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Council {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "academic_year", nullable = false)
	private String academicYear;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private Boolean isActive = true;

	@Column(name = "join_code", nullable = false, unique = true)
	private String joinCode;

	@PrePersist
	public void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
