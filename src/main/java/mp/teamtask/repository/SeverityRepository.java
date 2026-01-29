package mp.teamtask.repository;

import mp.teamtask.domain.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeverityRepository extends JpaRepository<Severity, Long> {
    Optional<Severity> findBySeverity(String severity);
}