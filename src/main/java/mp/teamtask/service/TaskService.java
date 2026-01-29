package mp.teamtask.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.*;
import mp.teamtask.dto.TaskDTO;
import mp.teamtask.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskStageRepository taskStageRepository;
    private final SeverityRepository severityRepository;
    private final FixVersionRepository fixVersionRepository;
    private final TaskTypeRepository taskTypeRepository;

    public void createTask(Task task, Long assigneeId) {
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            task.setAssignee(assignee);
        }

        if (task.getStage() == null) {
            TaskStage newStage = taskStageRepository.findByName("NEW")
                    .orElseThrow(() -> new RuntimeException("Default stage NEW not found"));
            task.setStage(newStage);
        }
        taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @Transactional
    public void updateTask(Long id, TaskDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());

        if (dto.getStageId() != null) {
            TaskStage stage = taskStageRepository.findById(dto.getStageId())
                    .orElseThrow(() -> new RuntimeException("Stage not found"));
            task.setStage(stage);
        }

        if (dto.getAssigneeId() != null) {
            User user = userRepository.findById(dto.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            task.setAssignee(user);
        } else {
            task.setAssignee(null);
        }

        if (dto.getSeverityId() != null) {
            Severity severity = severityRepository.findById(dto.getSeverityId())
                    .orElseThrow(() -> new RuntimeException("Severity not found"));
            task.setSeverity(severity);
        } else {
            task.setSeverity(null);
        }

        if (dto.getFixVersionId() != null) {
            FixVersion fixVersion = fixVersionRepository.findById(dto.getFixVersionId())
                    .orElseThrow(() -> new RuntimeException("FixVersion not found"));
            task.setFixVersion(fixVersion);
        } else {
            task.setFixVersion(null);
        }

        if (dto.getTaskTypeId() != null) {
            TaskType taskType = taskTypeRepository.findById(dto.getTaskTypeId())
                    .orElseThrow(() -> new RuntimeException("TaskType not found"));
            task.setTaskType(taskType);
        } else {
            task.setTaskType(null);
        }

        taskRepository.save(task);
    }

    public Task assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        task.setAssignee(user);
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public List<Task> getTasksByAssignee(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return taskRepository.findByAssignee(user);
    }

    public List<Task> getFilteredTasks(Long assigneeId, LocalDate start, LocalDate end,
                                       Long severityId, Long taskTypeId, Long fixVersionId) {
        List<Task> allTasks = taskRepository.findAll();

        return allTasks.stream()
                .filter(t -> assigneeId == null || (t.getAssignee() != null && t.getAssignee().getId().equals(assigneeId)))
                .filter(t -> start == null || !t.getCreatedAt().toLocalDate().isBefore(start))
                .filter(t -> end == null || !t.getCreatedAt().toLocalDate().isAfter(end))
                .filter(t -> severityId == null || (t.getSeverity() != null && t.getSeverity().getId().equals(severityId)))
                .filter(t -> taskTypeId == null || (t.getTaskType() != null && t.getTaskType().getId().equals(taskTypeId)))
                .filter(t -> fixVersionId == null || (t.getFixVersion() != null && t.getFixVersion().getId().equals(fixVersionId)))
                .collect(Collectors.toList());
    }
}