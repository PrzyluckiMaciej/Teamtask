package mp.teamtask.repository;

import mp.teamtask.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.assignedTasks WHERE u.email = :email")
    Optional<User> findByEmailWithTasks(@Param("email") String email);

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}