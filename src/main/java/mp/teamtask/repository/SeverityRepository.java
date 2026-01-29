package mp.teamtask.repository;

import mp.teamtask.domain.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeverityRepository extends JpaRepository<Severity, Long> {
    Optional<Severity> findBySeverity(String severity);
    @Query("SELECT COUNT(t) FROM Task t WHERE t.severity.id = :severityId")
    long countTasksBySeverityId(@Param("severityId") Long severityId);
}