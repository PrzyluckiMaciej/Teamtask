package mp.teamtask.repository;

import mp.teamtask.domain.FixVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FixVersionRepository extends JpaRepository<FixVersion, Long> {
    Optional<FixVersion> findByVersion(String version);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.fixVersion.id = :fixVersionId")
    long countTasksByFixVersionId(@Param("fixVersionId") Long fixVersionId);
}