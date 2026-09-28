package vn.iotstar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import vn.iotstar.security.CustomUserDetailsService;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, CustomUserDetailsService userDetailsService)
            throws Exception {
        http
            .userDetailsService(userDetailsService)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login", "/register", "/verify-otp", "/resend-register-otp",
                        "/forgot-password", "/reset-password", "/css/**", "/js/**", "/images/**")
                    .permitAll()
                .requestMatchers("/dashboard", "/users/**")
                    .hasRole("ADMIN")
                .requestMatchers("/products/**")
                    .authenticated()
                .anyRequest()
                    .authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll());
        return http.build();
    }
}
