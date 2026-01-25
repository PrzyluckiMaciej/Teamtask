package mp.teamtask.repository;

import mp.teamtask.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.id = :roleId")
    long countUsersByRoleId(@Param("roleId") Long roleId);
}