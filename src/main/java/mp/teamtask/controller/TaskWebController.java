package mp.teamtask.controller;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.Task;
import mp.teamtask.domain.TaskStage;
import mp.teamtask.dto.TaskDTO;
import mp.teamtask.service.TaskService;
import mp.teamtask.service.TaskStageService;
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
    private final TaskStageService taskStageService;

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

        TaskStage initialStage = taskStageService.getOrCreateStage("NEW");
        task.setStage(initialStage);

        taskService.createTask(task, taskDTO.getAssigneeId());
        return "redirect:/dashboard";
    }

    @GetMapping("/{id}")
    public String taskDetails(@PathVariable Long id, Model model) {
        Task task = taskService.getTaskById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + id));

        model.addAttribute("task", task);
        model.addAttribute("stages", taskStageService.getAllStages());
        model.addAttribute("users", userService.getAllUsers());
        return "tasks/details";
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