package mp.teamtask.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.User;
import mp.teamtask.dto.AuthRequest;
import mp.teamtask.dto.UserDTO;
import mp.teamtask.security.JwtUtils; // New import
import mp.teamtask.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils; // Injected utility

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate the JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);
        User user = (User) authentication.getPrincipal();

        // Build a response object containing the token and user details
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("type", "Bearer");
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setRole(userDTO.getRole());

        User registeredUser = userService.registerUser(user);

        // Return the registered user details
        UserDTO responseDTO = new UserDTO();
        responseDTO.setId(registeredUser.getId());
        responseDTO.setEmail(registeredUser.getEmail());
        responseDTO.setFirstName(registeredUser.getFirstName());
        responseDTO.setLastName(registeredUser.getLastName());
        responseDTO.setRole(registeredUser.getRole());

        return ResponseEntity.ok(responseDTO);
    }
}