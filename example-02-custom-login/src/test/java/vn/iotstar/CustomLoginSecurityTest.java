package vn.iotstar;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        mockMvc.perform(login("user01", "123456"))
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("user01"));
        mockMvc.perform(login("user01@example.com", "123456"))
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("user01"));
    }

    @Test
    void correctAccountWithWrongPasswordCannotLogin() throws Exception {
        mockMvc.perform(login("user01@example.com", "wrong-password"))
                .andExpect(redirectedUrl("/login?error=true"))
                .andExpect(unauthenticated());
    }

    @Test
    void invalidAndDisabledAccountsCannotLogin() throws Exception {
        mockMvc.perform(login("unknown", "123456"))
                .andExpect(redirectedUrl("/login?error=true"))
                .andExpect(unauthenticated());

        Role role = roles.findByName("ROLE_USER").orElseThrow();
        users.save(User.builder().username("disabled").email("disabled@example.com")
                .password(encoder.encode("123456")).fullName("Disabled").role(role).enabled(false).build());

        mockMvc.perform(login("disabled", "123456"))
                .andExpect(redirectedUrl("/login?error=true"))
                .andExpect(unauthenticated());
    }

    @Test
    void userIsForbiddenButAdminPassesAdminSecurityRule() throws Exception {
        mockMvc.perform(get("/admin/test").with(user("user01").roles("USER"))).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/test").with(user("admin").roles("ADMIN"))).andExpect(status().isNotFound());
    }

    @Test
    void logoutUsesCsrf() throws Exception {
        mockMvc.perform(post("/logout").with(user("user01").roles("USER"))).andExpect(status().isForbidden());
        mockMvc.perform(post("/logout").with(user("user01").roles("USER")).with(csrf()))
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    void emailLoginPrincipalRendersAllHeaderFieldsAndFallbackAvatar() throws Exception {
        MvcResult login = mockMvc.perform(login("user01@example.com", "123456"))
                .andExpect(authenticated().withUsername("user01"))
                .andReturn();
        HttpSession session = login.getRequest().getSession(false);

        mockMvc.perform(get("/").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nguyễn Hữu Trung")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user01@example.com")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ROLE_USER")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("avatar-default.svg")));
    }

    @Test
    void customUserDetailsExposesExpectedFieldsAndAuthority() {
        CustomUserDetails details = new CustomUserDetails(
                7L, "member", "member@example.com", "encoded", "Member Name",
                null, "ROLE_USER", true);

        assertEquals(7L, details.getId());
        assertEquals("member", details.getUsername());
        assertEquals("member@example.com", details.getEmail());
        assertEquals("Member Name", details.getFullName());
        assertEquals("ROLE_USER", details.getRole());
        assertTrue(details.isEnabled());
        assertFalse(details.getAuthorities().isEmpty());
        GrantedAuthority authority = details.getAuthorities().iterator().next();
        assertEquals("ROLE_USER", authority.getAuthority());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder login(
            String usernameOrEmail, String password) {
        return post("/login").with(csrf()).param("username", usernameOrEmail).param("password", password);
    }
}
