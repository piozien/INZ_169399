package pl.su.su_backend.model.council;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.users.Users;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "council_members")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilMember {

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CouncilMemberId implements Serializable {
        @Column(name = "council_id")
        private UUID councilId;

        @Column(name = "user_id")
        private UUID userId;
    }

    @EmbeddedId
    private CouncilMemberId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleCode role;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("councilId")
    @JoinColumn(name = "council_id")
    private Council council;
}