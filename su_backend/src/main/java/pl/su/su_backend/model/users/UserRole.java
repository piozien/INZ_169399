package pl.su.su_backend.model.users;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.su.su_backend.model.roles.Role;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

	@Embeddable
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Id implements Serializable {
		private UUID userId;
		private UUID roleId;
	}

	@EmbeddedId
	private Id id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("userId")
	@JoinColumn(name = "user_id")
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("roleId")
	@JoinColumn(name = "role_id")
	private Role role;

	@Column(name = "assigned_at", nullable = false)
	private LocalDateTime assignedAt;

	@PrePersist
	public void onAssign() {
		if (assignedAt == null) {
			assignedAt = LocalDateTime.now();
		}
	}
}
