package mp.teamtask.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.Task;
import mp.teamtask.domain.enums.TaskStage;
import mp.teamtask.dto.TaskDTO;
import mp.teamtask.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROGRAMMER')")
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO taskDTO) {
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStage(taskDTO.getStage() != null ? taskDTO.getStage() : TaskStage.NEW);

        Task createdTask = taskService.createTask(task, taskDTO.getAssigneeId());
        return ResponseEntity.ok(convertToDTO(createdTask));
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<TaskDTO> tasks = taskService.getAllTasks().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @Valid @RequestBody TaskDTO taskDTO) {
        Task taskDetails = new Task();
        taskDetails.setTitle(taskDTO.getTitle());
        taskDetails.setDescription(taskDTO.getDescription());
        taskDetails.setStage(taskDTO.getStage());

        if (taskDTO.getAssigneeId() != null) {
            mp.teamtask.domain.User assignee = new mp.teamtask.domain.User();
            assignee.setId(taskDTO.getAssigneeId());
            taskDetails.setAssignee(assignee);
        }

        Task updatedTask = taskService.updateTask(id, taskDetails);
        return ResponseEntity.ok(convertToDTO(updatedTask));
    }

    @PatchMapping("/{id}/stage")
    public ResponseEntity<TaskDTO> updateTaskStage(@PathVariable Long id, @RequestParam TaskStage stage) {
        Task updatedTask = taskService.updateTaskStage(id, stage);
        return ResponseEntity.ok(convertToDTO(updatedTask));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskDTO> assignTask(@PathVariable Long id, @RequestParam Long userId) {
        Task assignedTask = taskService.assignTask(id, userId);
        return ResponseEntity.ok(convertToDTO(assignedTask));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assignee/{userId}")
    public ResponseEntity<List<TaskDTO>> getTasksByAssignee(@PathVariable Long userId) {
        List<TaskDTO> tasks = taskService.getTasksByAssignee(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/stage/{stage}")
    public ResponseEntity<List<TaskDTO>> getTasksByStage(@PathVariable TaskStage stage) {
        List<TaskDTO> tasks = taskService.getTasksByStage(stage).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tasks);
    }

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStage(task.getStage());

        if (task.getAssignee() != null) {
            dto.setAssigneeId(task.getAssignee().getId());
            dto.setAssigneeName(task.getAssignee().getFullName());
        }

        return dto;
    }
}