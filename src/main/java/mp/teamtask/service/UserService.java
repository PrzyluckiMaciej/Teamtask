package mp.teamtask.service;

import jakarta.transaction.Transactional;
import mp.teamtask.domain.Task;
import mp.teamtask.domain.User;
import mp.teamtask.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailWithTasks(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Only update if provided (not null and not empty)
        if (userDetails.getFirstName() != null && !userDetails.getFirstName().isEmpty()) {
            user.setFirstName(userDetails.getFirstName());
        }

        if (userDetails.getLastName() != null && !userDetails.getLastName().isEmpty()) {
            user.setLastName(userDetails.getLastName());
        }

        if (userDetails.getEmail() != null && !userDetails.getEmail().isEmpty()) {
            user.setEmail(userDetails.getEmail());
        }

        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getAssignedTasks() != null) {
            for (Task task : user.getAssignedTasks()) {
                task.setAssignee(null);
            }
        }

        userRepository.delete(user);
    }

    public long countAdmins() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole().getName().equalsIgnoreCase("Admin"))
                .count();
    }

    public boolean canDeleteUser(Long id) {
        User userToDelete = userRepository.findById(id).orElse(null);
        if (userToDelete == null) return false;

        // If the user is an admin, check if they are the last one
        if (userToDelete.getRole().getName().equalsIgnoreCase("Admin")) {
            return countAdmins() > 1;
        }
        return true;
    }
}