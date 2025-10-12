package pl.su.su_backend.model.event;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.su.su_backend.model.enums.EventParticipantRole;
import pl.su.su_backend.model.users.Users;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_participants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventParticipant {

	@Embeddable
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Id implements Serializable {
		private UUID eventId;
		private UUID userId;
	}

	@EmbeddedId
	private Id id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("eventId")
	@JoinColumn(name = "event_id")
	private Event event;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("userId")
	@JoinColumn(name = "user_id")
	private Users user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EventParticipantRole role;

	@Column(nullable = false)
	@Builder.Default
	private Boolean confirmed = false;

	@Column(name = "assigned_at", nullable = false)
	private LocalDateTime assignedAt;

	@PrePersist
	public void onAssign() {
		if (assignedAt == null) {
			assignedAt = LocalDateTime.now();
		}
	}
}
