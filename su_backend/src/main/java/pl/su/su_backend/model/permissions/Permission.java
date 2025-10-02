package pl.su.su_backend.model.permissions;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.su.su_backend.model.roles.Role;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(unique = true, nullable = false, length = 100)
	private String code; // USER_EDIT, EVENT_DELETE

	@Column(length = 255)
	private String description;

	@ManyToMany(mappedBy = "permissions")
	@Builder.Default
	private Set<Role> roles = new HashSet<>();
}
