package mp.teamtask.repository;

import mp.teamtask.domain.TaskStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskStageRepository extends JpaRepository<TaskStage, Long> {
    Optional<TaskStage> findByName(String name);
    List<TaskStage> findAllByOrderByPositionAsc();
}
