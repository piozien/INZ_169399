package pl.su.su_backend.repositories.council;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.su.su_backend.model.council.CouncilMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouncilMemberRepository extends JpaRepository<CouncilMember, CouncilMember.CouncilMemberId> {

    List<CouncilMember> findByCouncilId(UUID councilId);
    List<CouncilMember> findByIdUserId(UUID userId);

    Optional<CouncilMember> findByCouncilIdAndUserId(UUID targetCouncilId, UUID userId);
}
