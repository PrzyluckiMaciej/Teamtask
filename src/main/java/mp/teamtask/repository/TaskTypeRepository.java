package mp.teamtask.repository;

import mp.teamtask.domain.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskTypeRepository extends JpaRepository<TaskType, Long> {
    Optional<TaskType> findByType(String type);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.taskType.id = :taskTypeId")
    long countTasksByTaskTypeId(@Param("taskTypeId") Long taskTypeId);
}