package mp.teamtask.controller;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.TaskStage;
import mp.teamtask.service.TaskStageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manage/stages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TaskStageWebController {

    private final TaskStageService stageService;

    @GetMapping
    public String listStages(Model model) {
        List<TaskStage> stages = stageService.getAllStages();

        // Create a map of Stage ID -> Is In Use
        Map<Long, Boolean> usageMap = stages.stream()
                .collect(Collectors.toMap(
                        TaskStage::getId,
                        stage -> stageService.isStageInUse(stage.getId())
                ));

        model.addAttribute("stages", stages);
        model.addAttribute("usageMap", usageMap);
        return "manage/stages-list";
    }

    @PostMapping
    public String addStage(@ModelAttribute TaskStage stage) {
        stageService.getOrCreateStage(stage.getName(), stage.getColor(), false);
        return "redirect:/manage/stages";
    }

    @PostMapping("/default/{id}")
    public String setDefault(@PathVariable Long id) {
        stageService.setDefaultStage(id);
        return "redirect:/manage/stages";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("stage", stageService.getStageById(id));
        return "manage/stages-edit";
    }

    @PutMapping("/{id}")
    public String updateStage(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam String color,
                              RedirectAttributes redirectAttributes) {
        stageService.updateStage(id, name, color);
        redirectAttributes.addFlashAttribute("success", "Stage updated successfully.");
        return "redirect:/manage/stages";
    }

    @DeleteMapping("/{id}")
    public String deleteStage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            stageService.deleteStage(id);
            redirectAttributes.addFlashAttribute("success", "Stage deleted successfully.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manage/stages";
    }
}