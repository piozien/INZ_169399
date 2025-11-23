package pl.su.su_backend.repositories.classes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.su.su_backend.model.classes.Classes;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassesRepository extends JpaRepository<Classes, UUID> {
    Optional<Classes> findByName(String name);
    Optional<Classes> findByNameAndYear(String name, String year);
}
