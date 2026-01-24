package mp.teamtask.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.Task;
import mp.teamtask.domain.TaskStage;
import mp.teamtask.domain.User;
import mp.teamtask.dto.TaskDTO;
import mp.teamtask.repository.TaskRepository;
import mp.teamtask.repository.TaskStageRepository;
import mp.teamtask.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskStageRepository taskStageRepository;

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

        // Update Stage via database lookup
        if (dto.getStageId() != null) {
            TaskStage stage = taskStageRepository.findById(dto.getStageId())
                    .orElseThrow(() -> new RuntimeException("Stage not found"));
            task.setStage(stage);
        }

        // Update Assignee via database lookup
        if (dto.getAssigneeId() != null) {
            User user = userRepository.findById(dto.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            task.setAssignee(user);
        } else {
            task.setAssignee(null);
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
}