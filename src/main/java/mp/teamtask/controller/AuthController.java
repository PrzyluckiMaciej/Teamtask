package mp.teamtask.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.User;
import mp.teamtask.dto.AuthRequest;
import mp.teamtask.dto.UserDTO;
import mp.teamtask.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setRole(user.getRole());

        return ResponseEntity.ok(userDTO);
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

        UserDTO responseDTO = new UserDTO();
        responseDTO.setId(registeredUser.getId());
        responseDTO.setEmail(registeredUser.getEmail());
        responseDTO.setFirstName(registeredUser.getFirstName());
        responseDTO.setLastName(registeredUser.getLastName());
        responseDTO.setRole(registeredUser.getRole());

        return ResponseEntity.ok(responseDTO);
    }
}