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
import mp.teamtask.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final UserService userService;
    private final TaskService taskService;
    private final RoleService roleService;
    private final TaskStageService taskStageService;
    private final SeverityService severityService;
    private final FixVersionService fixVersionService;
    private final TaskTypeService taskTypeService;

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
        // Fetch all roles from the database and filter out "Admin"
        List<Role> allRoles = roleService.getAllRoles();
        List<Role> filteredRoles = allRoles.stream()
                .filter(role -> !role.getName().equalsIgnoreCase("Admin"))
                .collect(Collectors.toList());
        model.addAttribute("roles", filteredRoles);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserDTO userDTO,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        // Fetch roles for the form in case of errors
        List<Role> allRoles = roleService.getAllRoles();
        List<Role> filteredRoles = allRoles.stream()
                .filter(role -> !role.getName().equalsIgnoreCase("Admin"))
                .collect(Collectors.toList());
        model.addAttribute("roles", filteredRoles);

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            User user = new User();
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setPassword(userDTO.getPassword());

            // Get the requested role
            if (userDTO.getRoleId() != null) {
                Role role = roleService.getRoleById(userDTO.getRoleId());

                // SERVER-SIDE VALIDATION: Prevent registering as Admin
                if (role.getName().equalsIgnoreCase("Admin")) {
                    redirectAttributes.addFlashAttribute("error", "Cannot register with Admin role.");
                    return "redirect:/register";
                }

                user.setRole(role);
            }

            userService.registerUser(user);
            return "redirect:/login?success";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long severityId,
            @RequestParam(required = false) Long taskTypeId,
            @RequestParam(required = false) Long fixVersionId,
            Model model) {

        // Fetch tasks using the enhanced filtering
        List<Task> tasks = taskService.getFilteredTasks(
                assigneeId, startDate, endDate,
                severityId, taskTypeId, fixVersionId
        );

        model.addAttribute("tasks", tasks);
        model.addAttribute("stages", taskStageService.getAllStages());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("severities", severityService.getAllSeverities());
        model.addAttribute("taskTypes", taskTypeService.getAllTaskTypes());
        model.addAttribute("fixVersions", fixVersionService.getAllFixVersions());

        // Keep track of selected filters to persist them in the UI
        model.addAttribute("selectedAssigneeId", assigneeId);
        model.addAttribute("selectedStartDate", startDate);
        model.addAttribute("selectedEndDate", endDate);
        model.addAttribute("selectedSeverityId", severityId);
        model.addAttribute("selectedTaskTypeId", taskTypeId);
        model.addAttribute("selectedFixVersionId", fixVersionId);

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
    public String updateProfile(@Valid @ModelAttribute("user") ProfileDTO profileDTO,
                                BindingResult bindingResult,
                                Authentication authentication,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "profile";
        }

        // Custom validation for password confirmation
        if (profileDTO.getNewPassword() != null && !profileDTO.getNewPassword().isEmpty()) {
            if (!profileDTO.getNewPassword().equals(profileDTO.getConfirmPassword())) {
                bindingResult.addError(new FieldError("user", "confirmPassword",
                        "New password and confirmation do not match"));
                return "profile";
            }
        }

        User currentUser = (User) authentication.getPrincipal();

        try {
            // Update user in database
            User updatedUser = userService.updateProfile(
                    currentUser.getId(),
                    profileDTO.getFirstName(),
                    profileDTO.getLastName(),
                    profileDTO.getEmail(),
                    profileDTO.getCurrentPassword(),
                    profileDTO.getNewPassword()
            );

            boolean emailChanged = !currentUser.getEmail().equals(updatedUser.getEmail());

            // Update the current principal for immediate UI update
            currentUser.setFirstName(updatedUser.getFirstName());
            currentUser.setLastName(updatedUser.getLastName());

            if (emailChanged) {
                currentUser.setEmail(updatedUser.getEmail());

                // If email changed, we need to log out
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                SecurityContextHolder.clearContext();

                redirectAttributes.addFlashAttribute("success",
                        "Profile updated successfully. Please log in with your new email.");
                return "redirect:/login?emailChanged";
            } else {
                // Only update password in principal if it was changed
                if (profileDTO.getNewPassword() != null && !profileDTO.getNewPassword().isEmpty()) {
                    // Don't set the encoded password in the principal, just acknowledge it was changed
                }

                redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
                return "redirect:/profile";
            }

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
            return "redirect:/profile";
        }
    }

    private void refreshAuthentication(User updatedUser, HttpServletRequest request) {
        // Create a new authentication token with updated user details
        UserDetails userDetails = userService.loadUserByUsername(updatedUser.getEmail());
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(userDetails,
                        userDetails.getPassword(),
                        userDetails.getAuthorities());

        // Set the new authentication in the security context
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(newAuth);

        // Update the session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);
        }
    }
}