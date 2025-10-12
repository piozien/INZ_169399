package pl.su.su_backend.repositories.permission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.su.su_backend.model.permissions.Permission;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, java.util.UUID> {
    Optional<Permission> findByName(String name);
}
