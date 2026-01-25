package mp.teamtask.controller;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.Role;
import mp.teamtask.service.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manage/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleWebController {

    private final RoleService roleService;

    @GetMapping
    public String listRoles(Model model) {
        List<Role> roles = roleService.getAllRoles();

        Map<Long, Boolean> roleUsageMap = roles.stream()
                .collect(Collectors.toMap(
                        Role::getId,
                        role -> roleService.isRoleInUse(role.getId())
                ));

        model.addAttribute("roles", roles);
        model.addAttribute("roleUsageMap", roleUsageMap);
        return "manage/roles-list";
    }

    @PostMapping
    public String addRole(@RequestParam String name) {
        roleService.getOrCreateRole(name);
        return "redirect:/manage/roles";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Role role = roleService.getRoleById(id);

        if (role.getName().equalsIgnoreCase("Admin")) {
            redirectAttributes.addFlashAttribute("error", "The Admin role is system-protected and cannot be edited.");
            return "redirect:/manage/roles";
        }

        model.addAttribute("role", role);
        return "manage/roles-edit";
    }

    @PutMapping("/{id}")
    public String updateRole(@PathVariable Long id, @ModelAttribute Role role, RedirectAttributes redirectAttributes) {
        Role existingRole = roleService.getRoleById(id);

        if (existingRole.getName().equalsIgnoreCase("Admin")) {
            return "redirect:/manage/roles";
        }

        existingRole.setName(role.getName());
        roleService.saveRole(existingRole);
        return "redirect:/manage/roles";
    }

    @DeleteMapping("/{id}")
    public String deleteRole(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roleService.deleteRole(id);
            return "redirect:/manage/roles?deleted";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/manage/roles";
        }
    }
}
