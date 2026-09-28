package vn.iotstar.config;

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
    CommandLineRunner init(RoleRepository roles, UserRepository users, PasswordEncoder encoder) {
        return args -> {
            Role userRole = roles.findByName("ROLE_USER")
                    .orElseGet(() -> roles.save(Role.builder().name("ROLE_USER").build()));
            if (users.findByUsername("user01").isEmpty()) {
                users.save(User.builder()
                        .username("user01")
                        .email("user01@example.com")
                        .password(encoder.encode("123456"))
                        .fullName("Nguyễn Hữu Trung")
                        .images(null)
                        .role(userRole)
                        .enabled(true)
                        .build());
            }
        };
    }
}
