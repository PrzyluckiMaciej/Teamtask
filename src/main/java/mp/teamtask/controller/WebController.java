package mp.teamtask.controller;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.Role;
import mp.teamtask.domain.Task;
import mp.teamtask.domain.TaskStage;
import mp.teamtask.domain.User;
import mp.teamtask.dto.UserDTO;
import mp.teamtask.service.RoleService;
import mp.teamtask.service.TaskStageService;
import mp.teamtask.service.UserService;
import mp.teamtask.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final UserService userService;
    private final TaskService taskService;
    private final RoleService roleService;
    private final TaskStageService taskStageService;

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
    public String dashboard(@RequestParam(required = false) boolean myTasks, Model model, Authentication authentication) {
        List<Task> tasks;
        if (myTasks) {
            User currentUser = (User) authentication.getPrincipal();
            tasks = taskService.getTasksByAssignee(currentUser.getId());
        } else {
            tasks = taskService.getAllTasks();
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("stages", taskStageService.getAllStages());
        return "dashboard";
    }
}