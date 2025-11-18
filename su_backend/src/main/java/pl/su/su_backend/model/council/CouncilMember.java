package pl.su.su_backend.model.council;

import jakarta.persistence.*;
import lombok.*;
import pl.su.su_backend.model.users.Users;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "council_members")
@Getter
@Setter
@NoArgsConstructor
public class CouncilMember {

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {
        @Column(name = "council_id")
        private UUID councilId;

        @Column(name = "user_id")
        private UUID userId;
    }

    @EmbeddedId
    private Id id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("councilId")
    @JoinColumn(name = "council_id")
    private Council council;
}
