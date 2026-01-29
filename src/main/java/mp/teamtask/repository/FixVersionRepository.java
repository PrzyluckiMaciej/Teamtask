package mp.teamtask.repository;

import mp.teamtask.domain.FixVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FixVersionRepository extends JpaRepository<FixVersion, Long> {
    Optional<FixVersion> findByVersion(String version);
}