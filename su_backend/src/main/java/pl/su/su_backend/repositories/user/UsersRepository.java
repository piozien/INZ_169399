package pl.su.su_backend.repositories.user;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.su.su_backend.model.users.Users;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UsersRepository extends JpaRepository<Users, UUID> {

    Optional<Users> findByEmail(String email);

    List<Users> findByClasses_Id(UUID classId);
}
