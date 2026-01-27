package mp.teamtask.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.Role;
import mp.teamtask.domain.Task;
import mp.teamtask.domain.TaskStage;
import mp.teamtask.domain.User;
import mp.teamtask.dto.ProfileDTO;
import mp.teamtask.dto.UserDTO;
import mp.teamtask.service.RoleService;
import mp.teamtask.service.TaskStageService;
import mp.teamtask.service.UserService;
import mp.teamtask.service.TaskService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final UserService userService;
    private final TaskService taskService;
    private final RoleService roleService;
    private final TaskStageService taskStageService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // renders templates/login.html
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserDTO());
        // Fetch all roles from the database instead of the Enum
        model.addAttribute("roles", roleService.getAllRoles());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserDTO userDTO) {
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());

        // Fetch the Role entity using the ID from the DTO
        if (userDTO.getRoleId() != null) {
            Role role = roleService.getRoleById(userDTO.getRoleId());
            user.setRole(role);
        }

        userService.registerUser(user);
        return "redirect:/login?success";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        // Fetch tasks using a flexible filtering approach
        List<Task> tasks = taskService.getFilteredTasks(assigneeId, startDate, endDate);

        model.addAttribute("tasks", tasks);
        model.addAttribute("stages", taskStageService.getAllStages());
        model.addAttribute("users", userService.getAllUsers());

        // Keep track of selected filters to persist them in the UI
        model.addAttribute("selectedAssigneeId", assigneeId);
        model.addAttribute("selectedStartDate", startDate);
        model.addAttribute("selectedEndDate", endDate);

        return "dashboard";
    }

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        User user = (User) authentication.getPrincipal();

        // Create a DTO for the form
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setFirstName(user.getFirstName());
        profileDTO.setLastName(user.getLastName());
        profileDTO.setEmail(user.getEmail());

        model.addAttribute("user", profileDTO);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") ProfileDTO profileDTO,
                                Authentication authentication,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        User currentUser = (User) authentication.getPrincipal();
        User user = userService.getUserById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean emailChanged = false;

        try {
            // Update basic info
            user.setFirstName(profileDTO.getFirstName());
            user.setLastName(profileDTO.getLastName());

            // Check if email is being changed
            if (!user.getEmail().equals(profileDTO.getEmail())) {
                if (userService.getUserByEmail(profileDTO.getEmail()).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Email already exists");
                    return "redirect:/profile";
                }
                user.setEmail(profileDTO.getEmail());
                emailChanged = true;
            }

            // Handle password change
            if (profileDTO.getNewPassword() != null && !profileDTO.getNewPassword().isEmpty()) {
                if (profileDTO.getCurrentPassword() == null || profileDTO.getCurrentPassword().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Current password is required to change password");
                    return "redirect:/profile";
                }

                if (!profileDTO.getNewPassword().equals(profileDTO.getConfirmPassword())) {
                    redirectAttributes.addFlashAttribute("error", "New passwords do not match");
                    return "redirect:/profile";
                }

                // Verify current password
                if (!passwordEncoder.matches(profileDTO.getCurrentPassword(), user.getPassword())) {
                    redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
                    return "redirect:/profile";
                }

                // Update password
                user.setPassword(passwordEncoder.encode(profileDTO.getNewPassword()));
            }

            userService.updateUser(user.getId(), user);

            // If email was changed, log the user out
            if (emailChanged) {
                // Invalidate session
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }

                // Clear security context
                SecurityContextHolder.clearContext();

                redirectAttributes.addFlashAttribute("success",
                        "Profile updated successfully. Please log in with your new email.");
                return "redirect:/login?emailChanged";
            }

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
        }

        return "redirect:/profile";
    }
}