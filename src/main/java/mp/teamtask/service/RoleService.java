package mp.teamtask.service;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.Role;
import mp.teamtask.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + id));
    }

    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Role not found: " + name));
    }

    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    public boolean isRoleInUse(Long id) {
        return roleRepository.countUsersByRoleId(id) > 0;
    }

    public void deleteRole(Long id) {
        if (isRoleInUse(id)) {
            throw new IllegalStateException("Role is currently assigned to users and cannot be deleted.");
        }
        roleRepository.deleteById(id);
    }

    // Helper for DataInitializer to avoid exceptions during startup
    public Role getOrCreateRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(null, name)));
    }
}