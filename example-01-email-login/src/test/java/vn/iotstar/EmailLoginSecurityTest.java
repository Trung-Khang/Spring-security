package vn.iotstar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ActiveProfiles("test")
@SpringBootTest(properties = {"ADMIN_EMAIL=admin@example.com", "ADMIN_PASSWORD=change-me"})
class EmailLoginSecurityTest {
    @Autowired private WebApplicationContext context;
    @Autowired private UserRepository users;
    @Autowired private PasswordEncoder passwordEncoder;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("auth/login"));
    }

    @Test
    void homeIsPublic() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
    }

    @Test
    void emailLoginSucceedsAndAuthenticatesEmail() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("email", "admin@example.com").param("password", "change-me"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("admin@example.com"));
    }

    @Test
    void wrongPasswordFailsLogin() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("email", "admin@example.com").param("password", "wrong-password"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?error=true"))
                .andExpect(unauthenticated());
    }

    @Test
    void anonymousAdminRequestRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/test")).andExpect(status().is3xxRedirection());
    }

    @Test
    void nonAdminCannotAccessAdminRequest() throws Exception {
        mockMvc.perform(get("/admin/test").with(user("member").roles("USER"))).andExpect(status().isForbidden());
    }

    @Test
    void logoutRequiresCsrfAndSucceedsWithToken() throws Exception {
        mockMvc.perform(post("/logout").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mockMvc.perform(post("/logout").with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    void seededPasswordUsesBcrypt() {
        User admin = users.findByEmailIgnoreCase("admin@example.com").orElseThrow();
        assertTrue(passwordEncoder.matches("change-me", admin.getPassword()));
    }
}
