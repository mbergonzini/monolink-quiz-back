package monolink.monolinkquizback.repository;

import monolink.monolinkquizback.entity.ERole;
import monolink.monolinkquizback.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
