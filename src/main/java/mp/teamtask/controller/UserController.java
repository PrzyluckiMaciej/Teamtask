package mp.teamtask.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.Role;
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
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "manage/users/users-list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getAllRoles());
        return "manage/users/users-edit";
    }

    @PutMapping("/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute("user") User userDetails,
                             @RequestParam(value = "roleId", required = false) Long roleId,
                             Authentication authentication,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        User currentUser = (User) authentication.getPrincipal();
        User existingUser = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        boolean roleChanged = false;
        boolean selfRoleChange = currentUser.getId().equals(id);

        // Check if trying to change the role
        if (roleId != null) {
            Role newRole = roleService.getRoleById(roleId);

            // Check if the role is actually changing
            if (!existingUser.getRole().getId().equals(newRole.getId())) {
                roleChanged = true;

                // Check if the user is currently an admin
                boolean isCurrentAdmin = existingUser.getRole().getName().equalsIgnoreCase("Admin");
                boolean isChangingToAdmin = newRole.getName().equalsIgnoreCase("Admin");

                // If changing from admin to non-admin
                if (isCurrentAdmin && !isChangingToAdmin) {
                    // Check if this is the last admin
                    long adminCount = userService.countAdmins();

                    if (adminCount <= 1) {
                        redirectAttributes.addFlashAttribute("error",
                                "Cannot change role: This is the only administrator account. " +
                                        "Ensure another admin exists before changing roles.");
                        return "redirect:/manage/users";
                    }
                }

                // Set the new role
                existingUser.setRole(newRole);
            }
        }

        // Update other fields
        if (userDetails.getFirstName() != null && !userDetails.getFirstName().isEmpty()) {
            existingUser.setFirstName(userDetails.getFirstName());
        }

        if (userDetails.getLastName() != null && !userDetails.getLastName().isEmpty()) {
            existingUser.setLastName(userDetails.getLastName());
        }

        if (userDetails.getEmail() != null && !userDetails.getEmail().isEmpty()) {
            existingUser.setEmail(userDetails.getEmail());
        }

        userService.saveUser(existingUser);

        // If the current user changed their own role AND they're no longer admin, log them out
        if (selfRoleChange && roleChanged) {
            Role newRole = existingUser.getRole();
            if (!newRole.getName().equalsIgnoreCase("Admin")) {
                // Invalidate session and redirect to login
                SecurityContextHolder.clearContext();
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }

                redirectAttributes.addFlashAttribute("success",
                        "Your role has been changed. Please log in with your new permissions.");
                return "redirect:/login?roleChanged";
            }
        }

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
        return "manage/users/users-create";
    }

    @PostMapping
    public String registerNewUser(@ModelAttribute("user") User user, @RequestParam Long roleId) {
        user.setRole(roleService.getRoleById(roleId));
        userService.registerUser(user);
        return "redirect:/manage/users?created";
    }
}