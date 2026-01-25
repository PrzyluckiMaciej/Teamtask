package mp.teamtask.controller;

import lombok.RequiredArgsConstructor;
import mp.teamtask.service.RoleService;
import mp.teamtask.service.TaskStageService;
import mp.teamtask.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manage")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ManagementController {
    private final UserService userService;
    private final RoleService roleService;
    private final TaskStageService stageService;

    @GetMapping
    public String manageDashboard() {
        return "manage/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("items", userService.getAllUsers());
        model.addAttribute("type", "users");
        return "manage/list";
    }

    @GetMapping("/roles")
    public String manageRoles(Model model) {
        model.addAttribute("items", roleService.getAllRoles());
        model.addAttribute("type", "roles");
        return "manage/list";
    }

    @GetMapping("/stages")
    public String manageStages(Model model) {
        model.addAttribute("items", stageService.getAllStages());
        model.addAttribute("type", "stages");
        return "manage/list";
    }
}
