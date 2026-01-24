package mp.teamtask.controller;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.User;
import mp.teamtask.dto.UserDTO;
import mp.teamtask.service.UserService;
import mp.teamtask.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final UserService userService;
    private final TaskService taskService;

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
        // Pass roles to the view so the user can select one
        model.addAttribute("roles", mp.teamtask.domain.enums.Role.values());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserDTO userDTO) {
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setRole(userDTO.getRole());

        userService.registerUser(user);
        return "redirect:/login?success";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("tasks", taskService.getAllTasks());
        return "dashboard";
    }
}