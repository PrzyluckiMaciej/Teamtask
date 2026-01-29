package mp.teamtask.controller;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.Severity;
import mp.teamtask.service.SeverityService;
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
@RequestMapping("/manage/severities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SeverityController {

    private final SeverityService severityService;

    @GetMapping
    public String listSeverities(Model model) {
        List<Severity> severities = severityService.getAllSeverities();

        Map<Long, Boolean> severityUsageMap = severities.stream()
                .collect(Collectors.toMap(
                        Severity::getId,
                        severity -> severityService.isSeverityInUse(severity.getId())
                ));

        model.addAttribute("severities", severities);
        model.addAttribute("severityUsageMap", severityUsageMap);
        return "manage/severities/severities-list";
    }

    @PostMapping
    public String addSeverity(@RequestParam String severity,
                              @RequestParam String color,
                              RedirectAttributes redirectAttributes) {
        // Check if severity exists (case-insensitive)
        Optional<Severity> existing = severityService.findBySeverity(severity);

        if (existing.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "A severity with the name '" + severity + "' already exists.");
            return "redirect:/manage/severities";
        }

        severityService.saveSeverity(new Severity(null, severity, color));
        redirectAttributes.addFlashAttribute("success", "Severity '" + severity + "' added successfully.");
        return "redirect:/manage/severities";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Severity severity = severityService.getSeverityById(id);
        model.addAttribute("severity", severity);
        return "manage/severities/severities-edit";
    }

    @PutMapping("/{id}")
    public String updateSeverity(@PathVariable Long id,
                                 @RequestParam("severity") String severityName,
                                 @RequestParam("color") String color,
                                 RedirectAttributes redirectAttributes) {

        try {
            Severity existingSeverity = severityService.getSeverityById(id);

            // Check if the new name already exists (excluding current)
            Optional<Severity> existingWithName = severityService.findBySeverity(severityName);
            if (existingWithName.isPresent() && !existingWithName.get().getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "A severity with the name '" + severityName + "' already exists.");
                return "redirect:/manage/severities/edit/" + id;
            }

            existingSeverity.setSeverity(severityName);
            existingSeverity.setColor(color);
            severityService.saveSeverity(existingSeverity);

            redirectAttributes.addFlashAttribute("success", "Severity updated successfully.");
            return "redirect:/manage/severities";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating severity: " + e.getMessage());
            return "redirect:/manage/severities/edit/" + id;
        }
    }

    @DeleteMapping("/{id}")
    public String deleteSeverity(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            severityService.deleteSeverity(id);
            redirectAttributes.addFlashAttribute("success", "Severity deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete severity: " + e.getMessage());
        }
        return "redirect:/manage/severities";
    }
}