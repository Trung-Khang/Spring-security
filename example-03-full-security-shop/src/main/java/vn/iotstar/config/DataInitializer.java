package vn.iotstar.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initializeRolesAndAdmin(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ADMIN_USERNAME:admin}") String adminUsername,
            @Value("${ADMIN_EMAIL:admin@example.com}") String adminEmail,
            @Value("${ADMIN_PASSWORD:change-me}") String adminPassword) {
        return args -> initialize(roleRepository, userRepository, passwordEncoder,
                adminUsername, adminEmail, adminPassword);
    }

    void initialize(RoleRepository roles, UserRepository users, PasswordEncoder encoder,
            String username, String email, String password) {
        roles.findByName("ROLE_USER").orElseGet(() -> roles.save(new Role("ROLE_USER")));
        Role adminRole = roles.findByName("ROLE_ADMIN").orElseGet(() -> roles.save(new Role("ROLE_ADMIN")));
        if (!users.existsByUsername(username) && !users.existsByEmail(email)) {
            User admin = new User();
            admin.setUsername(username);
            admin.setEmail(email);
            admin.setFullName("Administrator");
            admin.setPassword(encoder.encode(password));
            admin.setRole(adminRole);
            admin.setEnabled(true);
            users.save(admin);
        }
    }
}
