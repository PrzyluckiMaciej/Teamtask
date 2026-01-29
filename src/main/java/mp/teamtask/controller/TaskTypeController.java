package mp.teamtask.controller;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.TaskType;
import mp.teamtask.service.TaskTypeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manage/tasktypes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TaskTypeController {

    private final TaskTypeService taskTypeService;

    @GetMapping
    public String listTaskTypes(Model model) {
        List<TaskType> taskTypes = taskTypeService.getAllTaskTypes();

        Map<Long, Boolean> taskTypeUsageMap = taskTypes.stream()
                .collect(Collectors.toMap(
                        TaskType::getId,
                        taskType -> taskTypeService.isTaskTypeInUse(taskType.getId())
                ));

        model.addAttribute("taskTypes", taskTypes);
        model.addAttribute("taskTypeUsageMap", taskTypeUsageMap);
        return "manage/tasktypes/tasktypes-list";
    }

    @PostMapping
    public String addTaskType(@RequestParam String type, RedirectAttributes redirectAttributes) {
        Optional<TaskType> existing = taskTypeService.findByType(type);

        if (existing.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "A task type with the name '" + type + "' already exists.");
            return "redirect:/manage/tasktypes";
        }

        taskTypeService.saveTaskType(new TaskType(null, type));
        redirectAttributes.addFlashAttribute("success", "Task type '" + type + "' added successfully.");
        return "redirect:/manage/tasktypes";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        TaskType taskType = taskTypeService.getTaskTypeById(id);
        model.addAttribute("taskType", taskType);
        return "manage/tasktypes/tasktypes-edit";
    }

    @PutMapping("/{id}")
    public String updateTaskType(@PathVariable Long id,
                                 @RequestParam("type") String typeName,
                                 RedirectAttributes redirectAttributes) {

        try {
            TaskType existingTaskType = taskTypeService.getTaskTypeById(id);

            Optional<TaskType> existingWithType = taskTypeService.findByType(typeName);
            if (existingWithType.isPresent() && !existingWithType.get().getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "A task type with the name '" + typeName + "' already exists.");
                return "redirect:/manage/tasktypes/edit/" + id;
            }

            existingTaskType.setType(typeName);
            taskTypeService.saveTaskType(existingTaskType);

            redirectAttributes.addFlashAttribute("success", "Task type updated successfully.");
            return "redirect:/manage/tasktypes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating task type: " + e.getMessage());
            return "redirect:/manage/tasktypes/edit/" + id;
        }
    }

    @DeleteMapping("/{id}")
    public String deleteTaskType(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            taskTypeService.deleteTaskType(id);
            redirectAttributes.addFlashAttribute("success", "Task type deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete task type: " + e.getMessage());
        }
        return "redirect:/manage/tasktypes";
    }
}