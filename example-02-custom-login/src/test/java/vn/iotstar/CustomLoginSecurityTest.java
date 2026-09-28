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
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
class CustomLoginSecurityTest {
    @Autowired private WebApplicationContext context;
    @Autowired private UserRepository users;
    @Autowired private RoleRepository roles;
    @Autowired private PasswordEncoder encoder;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void loginPageIsPublicAndHomeRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/")).andExpect(status().is3xxRedirection());
    }

    @Test
    void usernameAndEmailCanBothLogin() throws Exception {
        mockMvc.perform(post("/login").with(csrf()).param("username", "user01").param("password", "123456"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("user01"));
        mockMvc.perform(post("/login").with(csrf()).param("username", "user01@example.com").param("password", "123456"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("user01"));
    }

    @Test
    void invalidAndDisabledAccountsCannotLogin() throws Exception {
        mockMvc.perform(post("/login").with(csrf()).param("username", "unknown").param("password", "123456"))
                .andExpect(redirectedUrl("/login?error=true")).andExpect(unauthenticated());
        Role role = roles.findByName("ROLE_USER").orElseThrow();
        users.save(User.builder().username("disabled").email("disabled@example.com")
                .password(encoder.encode("123456")).fullName("Disabled").role(role).enabled(false).build());
        mockMvc.perform(post("/login").with(csrf()).param("username", "disabled").param("password", "123456"))
                .andExpect(redirectedUrl("/login?error=true")).andExpect(unauthenticated());
    }

    @Test
    void userRoleIsForbiddenFromAdminAndLogoutUsesCsrf() throws Exception {
        mockMvc.perform(get("/admin/test").with(user("user01").roles("USER"))).andExpect(status().isForbidden());
        mockMvc.perform(post("/logout").with(user("user01").roles("USER"))).andExpect(status().isForbidden());
        mockMvc.perform(post("/logout").with(user("user01").roles("USER")).with(csrf()))
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    void customPrincipalRendersFullNameAndDefaultAvatar() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(1L, "user01", "user01@example.com",
                "ignored", "Nguyễn Hữu Trung", null, "ROLE_USER", true);
        mockMvc.perform(get("/").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nguyễn Hữu Trung")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("avatar-default.svg")));
    }
}
