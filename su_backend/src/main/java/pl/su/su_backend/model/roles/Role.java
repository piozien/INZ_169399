package pl.su.su_backend.model.roles;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.su.su_backend.model.enums.RoleCategory;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.permissions.Permission;
import pl.su.su_backend.model.users.UserRole;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(unique = true, nullable = false)
	private RoleCode roleCode;
	private String description;
    
	@ManyToMany
	@JoinTable(
		name = "role_permissions",
		joinColumns = @JoinColumn(name = "role_id"),
		inverseJoinColumns = @JoinColumn(name = "permission_id")
	)
	@Builder.Default
	private Set<Permission> permissions = new HashSet<>();

	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<UserRole> userRoles = new HashSet<>();


	public String getDisplayName() {
		return roleCode != null ? roleCode.getDisplayName() : null;
	}


	public RoleCategory getCategory() {
		return roleCode != null ? roleCode.getCategory() : null;
	}
}
