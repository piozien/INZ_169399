package pl.su.su_backend.model.users;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.StatusEnum;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusEnum status = StatusEnum.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", referencedColumnName = "id")
    private Classes classes;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    private AuthProvider authProvider;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "refresh_token")
    private String refreshToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = StatusEnum.PENDING;
        }
    }

    public boolean isBlocked() {
        return StatusEnum.BLOCKED.equals(this.status);
    }

}
