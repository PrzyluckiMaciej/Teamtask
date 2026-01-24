package mp.teamtask.controller;

import lombok.RequiredArgsConstructor;
import mp.teamtask.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Entire controller restricted to ADMIN
public class UserWebController {

    private final UserService userService;

    // Display a list of all users
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "users/list"; // renders templates/users/list.html
    }

    // View specific user details
    @GetMapping("/{id}")
    public String userDetails(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "users/view";
    }

    // Delete a user
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }
}