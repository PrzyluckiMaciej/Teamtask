package mp.teamtask.config;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.Role;
import mp.teamtask.domain.User;
import mp.teamtask.service.RoleService;
import mp.teamtask.service.TaskStageService;
import mp.teamtask.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleService roleService;
    private final UserService userService;
    private final TaskStageService stageService;

    @Override
    public void run(String... args) throws Exception {
        Role adminRole = roleService.getOrCreateRole("Admin");
        Role programmerRole = roleService.getOrCreateRole("Programmer");
        Role testerRole = roleService.getOrCreateRole("Tester");

        // Create admin user if not exists
        if (userService.getUserByEmail("admin@teamtask.com").isEmpty()) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@teamtask.com");
            admin.setPassword("admin123");
            admin.setRole(adminRole);

            userService.registerUser(admin);
            System.out.println("Admin user created: admin@teamtask.com / admin123");
        }

        // Create sample programmer
        if (userService.getUserByEmail("programmer@teamtask.com").isEmpty()) {
            User programmer = new User();
            programmer.setFirstName("John");
            programmer.setLastName("Programmer");
            programmer.setEmail("programmer@teamtask.com");
            programmer.setPassword("programmer123");
            programmer.setRole(programmerRole);

            userService.registerUser(programmer);
            System.out.println("Programmer user created: programmer@teamtask.com / programmer123");
        }

        // Create sample tester
        if (userService.getUserByEmail("tester@teamtask.com").isEmpty()) {
            User tester = new User();
            tester.setFirstName("Jane");
            tester.setLastName("Tester");
            tester.setEmail("tester@teamtask.com");
            tester.setPassword("tester123");
            tester.setRole(testerRole);

            userService.registerUser(tester);
            System.out.println("Tester user created: tester@teamtask.com / tester123");
        }

        stageService.getOrCreateStage("NEW");
        stageService.getOrCreateStage("IN_PROGRESS");
        stageService.getOrCreateStage("TESTING");
        stageService.getOrCreateStage("COMPLETED");
    }
}