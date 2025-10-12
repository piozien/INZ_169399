package pl.su.su_backend.repositories.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.enums.RoleCode;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, java.util.UUID> {
    Optional<Role> findByRoleCode(RoleCode roleCode);
}
