package mp.teamtask.controller;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.FixVersion;
import mp.teamtask.service.FixVersionService;
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
@RequestMapping("/manage/fixversions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FixVersionController {

    private final FixVersionService fixVersionService;

    @GetMapping
    public String listFixVersions(Model model) {
        List<FixVersion> fixVersions = fixVersionService.getAllFixVersions();

        Map<Long, Boolean> fixVersionUsageMap = fixVersions.stream()
                .collect(Collectors.toMap(
                        FixVersion::getId,
                        fixVersion -> fixVersionService.isFixVersionInUse(fixVersion.getId())
                ));

        model.addAttribute("fixVersions", fixVersions);
        model.addAttribute("fixVersionUsageMap", fixVersionUsageMap);
        return "manage/fixversions/fixversions-list";
    }

    @PostMapping
    public String addFixVersion(@RequestParam String version, RedirectAttributes redirectAttributes) {
        Optional<FixVersion> existing = fixVersionService.findByVersion(version);

        if (existing.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "A fix version with the name '" + version + "' already exists.");
            return "redirect:/manage/fixversions";
        }

        fixVersionService.saveFixVersion(new FixVersion(null, version));
        redirectAttributes.addFlashAttribute("success", "Fix version '" + version + "' added successfully.");
        return "redirect:/manage/fixversions";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        FixVersion fixVersion = fixVersionService.getFixVersionById(id);
        model.addAttribute("fixVersion", fixVersion);
        return "manage/fixversions/fixversions-edit";
    }

    @PutMapping("/{id}")
    public String updateFixVersion(@PathVariable Long id,
                                   @RequestParam("version") String versionName,
                                   RedirectAttributes redirectAttributes) {

        try {
            FixVersion existingFixVersion = fixVersionService.getFixVersionById(id);

            Optional<FixVersion> existingWithVersion = fixVersionService.findByVersion(versionName);
            if (existingWithVersion.isPresent() && !existingWithVersion.get().getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "A fix version with the name '" + versionName + "' already exists.");
                return "redirect:/manage/fixversions/edit/" + id;
            }

            existingFixVersion.setVersion(versionName);
            fixVersionService.saveFixVersion(existingFixVersion);

            redirectAttributes.addFlashAttribute("success", "Fix version updated successfully.");
            return "redirect:/manage/fixversions";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating fix version: " + e.getMessage());
            return "redirect:/manage/fixversions/edit/" + id;
        }
    }

    @DeleteMapping("/{id}")
    public String deleteFixVersion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            fixVersionService.deleteFixVersion(id);
            redirectAttributes.addFlashAttribute("success", "Fix version deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete fix version: " + e.getMessage());
        }
        return "redirect:/manage/fixversions";
    }
}