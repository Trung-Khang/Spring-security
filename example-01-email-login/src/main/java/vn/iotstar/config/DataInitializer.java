package vn.iotstar.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.*;
@Configuration public class DataInitializer {
 @Bean CommandLineRunner init(RoleRepository roles,UserRepository users,PasswordEncoder encoder,@Value("${ADMIN_EMAIL:admin@example.com}")String email,@Value("${ADMIN_PASSWORD:change-me}")String password){return args->{
   Role user=roles.findByNameIgnoreCase("USER").orElseGet(()->roles.save(new Role("USER")));
   Role admin=roles.findByNameIgnoreCase("ADMIN").orElseGet(()->roles.save(new Role("ADMIN")));
   if(!users.existsByEmailIgnoreCase(email)){users.save(new User(email.toLowerCase(),encoder.encode(password),"System Administrator",admin));}
 };}
}
