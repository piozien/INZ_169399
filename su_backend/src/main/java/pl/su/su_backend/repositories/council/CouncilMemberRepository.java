package pl.su.su_backend.repositories.council;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.model.users.Users;

import java.util.Optional;
import java.util.UUID;

public interface CouncilMemberRepository extends JpaRepository<CouncilMember, UUID> {
    Optional<CouncilMember> findByUser(Users user);
}
