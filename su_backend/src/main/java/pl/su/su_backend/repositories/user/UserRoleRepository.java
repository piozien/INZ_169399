package pl.su.su_backend.repositories.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.su.su_backend.model.users.UserRole;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.Id> {

    List<UserRole> findByRole_Id(UUID roleId);

    boolean existsByUser_IdAndRole_Id(UUID userId, UUID roleId);
}


