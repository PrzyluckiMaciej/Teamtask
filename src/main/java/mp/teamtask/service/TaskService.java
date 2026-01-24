package mp.teamtask.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.Task;
import mp.teamtask.domain.enums.TaskStage;
import mp.teamtask.domain.User;
import mp.teamtask.dto.TaskDTO;
import mp.teamtask.repository.TaskRepository;
import mp.teamtask.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public void createTask(Task task, Long assigneeId) {
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            task.setAssignee(assignee);
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
        task.setStage(dto.getStage()); // Update the stage here

        if (dto.getAssigneeId() != null) {
            User user = userRepository.findById(dto.getAssigneeId()).orElse(null);
            task.setAssignee(user);
        } else {
            task.setAssignee(null);
        }
    }

    public Task updateTaskStage(Long id, TaskStage stage) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.setStage(stage);
        return taskRepository.save(task);
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

    public List<Task> getTasksByStage(TaskStage stage) {
        return taskRepository.findByStage(stage);
    }
}