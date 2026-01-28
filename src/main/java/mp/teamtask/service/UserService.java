package mp.teamtask.service;

import jakarta.transaction.Transactional;
import mp.teamtask.domain.Role;
import mp.teamtask.domain.Task;
import mp.teamtask.domain.User;
import mp.teamtask.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
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

    public User updateUser(Long id, User userDetails, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean selfUpdate = currentUser.getId().equals(id);
        boolean roleChanged = false;

        // Check if trying to change role away from Admin when last admin
        boolean isCurrentAdmin = user.getRole().getName().equalsIgnoreCase("Admin");

        if (userDetails.getRole() != null) {
            Role newRole = userDetails.getRole();
            roleChanged = !user.getRole().getId().equals(newRole.getId());
            boolean isChangingFromAdmin = isCurrentAdmin && !newRole.getName().equalsIgnoreCase("Admin");

            if (isChangingFromAdmin) {
                long adminCount = userRepository.findAll().stream()
                        .filter(u -> u.getRole().getName().equalsIgnoreCase("Admin"))
                        .count();

                if (adminCount <= 1) {
                    throw new IllegalStateException(
                            "Cannot change role: This is the only administrator account. " +
                                    "Ensure another admin exists before changing roles.");
                }
            }

            user.setRole(newRole);
        }

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

        User savedUser = userRepository.save(user);

        // Return both the saved user and a flag indicating if role changed
        return savedUser;
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
        return userRepository.countAdmins();
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

    @Transactional
    public User updateProfile(Long userId, String firstName, String lastName, String email,
                              String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update name
        if (firstName != null && !firstName.isEmpty()) {
            user.setFirstName(firstName);
        }

        if (lastName != null && !lastName.isEmpty()) {
            user.setLastName(lastName);
        }

        // Check if email is being changed
        boolean emailChanged = false;
        if (email != null && !email.isEmpty() && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(email);
            emailChanged = true;
        }

        // Handle password change
        if (newPassword != null && !newPassword.isEmpty()) {
            if (currentPassword == null || currentPassword.isEmpty()) {
                throw new IllegalArgumentException("Current password is required to change password");
            }

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        return userRepository.save(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}