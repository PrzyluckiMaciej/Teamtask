package mp.teamtask.controller;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.Task;
import mp.teamtask.domain.enums.TaskStage;
import mp.teamtask.dto.TaskDTO;
import mp.teamtask.service.TaskService;
import mp.teamtask.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskWebController {

    private final TaskService taskService;
    private final UserService userService;

    // Show form to create a new task
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("task", new TaskDTO());
        model.addAttribute("users", userService.getAllUsers());
        return "tasks/create-task";
    }

    // Process the creation of a task
    @PostMapping
    public String createTask(@ModelAttribute("task") TaskDTO taskDTO) {
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStage(TaskStage.NEW);

        taskService.createTask(task, taskDTO.getAssigneeId());
        return "redirect:/dashboard";
    }

    // Update only the stage of a task (e.g., NEW -> IN_PROGRESS)
    @PatchMapping("/{id}/stage")
    public String updateTaskStage(@PathVariable Long id, @RequestParam TaskStage stage) {
        taskService.updateTaskStage(id, stage);
        return "redirect:/dashboard";
    }

    // Show form to edit an existing task
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Task task = taskService.getTaskById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + id));

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle(task.getTitle());
        taskDTO.setDescription(task.getDescription());
        taskDTO.setStage(task.getStage()); // Ensure TaskDTO has a 'stage' field

        if (task.getAssignee() != null) {
            taskDTO.setAssigneeId(task.getAssignee().getId());
        }

        model.addAttribute("task", taskDTO);
        model.addAttribute("taskId", id);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("stages", mp.teamtask.domain.enums.TaskStage.values());

        return "tasks/edit-task";
    }

    // Process the update using PUT
    @PutMapping("/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute("task") TaskDTO taskDTO) {
        taskService.updateTask(id, taskDTO);
        return "redirect:/dashboard";
    }

    // Delete a task using the Hidden Method Filter
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/dashboard";
    }
}