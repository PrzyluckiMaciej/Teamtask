package mp.teamtask.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.User;
import mp.teamtask.service.RoleService;
import mp.teamtask.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manage/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserWebController {

    private final UserService userService;
    private final RoleService roleService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "manage/users-list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getAllRoles());
        return "manage/users-edit";
    }

    @PutMapping("/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute("user") User userDetails) {
        userService.updateUser(id, userDetails);
        return "redirect:/manage/users";
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id,
                             HttpServletRequest request,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        User currentUser = (User) authentication.getPrincipal();

        if (!userService.canDeleteUser(id)) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete the last administrator.");
            return "redirect:/manage/users";
        }

        userService.deleteUser(id);

        if (currentUser.getId().equals(id)) {
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return "redirect:/login?deleted";
        }

        return "redirect:/manage/users?deleted";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User()); // Using Entity or UserDTO
        model.addAttribute("roles", roleService.getAllRoles());
        return "manage/users-create";
    }

    @PostMapping
    public String registerNewUser(@ModelAttribute("user") User user, @RequestParam Long roleId) {
        user.setRole(roleService.getRoleById(roleId));
        userService.registerUser(user);
        return "redirect:/manage/users?created";
    }
}