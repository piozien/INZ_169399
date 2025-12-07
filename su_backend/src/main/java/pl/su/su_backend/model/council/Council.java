package pl.su.su_backend.model.council;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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
    private boolean active = true;

    @Column(name = "is_default", nullable = false)
    private boolean defaultCouncil = false;

    @Column(name = "join_code", nullable = false, unique = true)
    private String joinCode;

    @OneToMany(mappedBy = "council", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<CouncilMember> members = new HashSet<>();

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}