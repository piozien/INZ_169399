package pl.su.su_backend.repositories.council;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.su.su_backend.model.council.Council;

import java.util.UUID;

@Repository
public interface CouncilRepository extends JpaRepository<Council, UUID> {
}
