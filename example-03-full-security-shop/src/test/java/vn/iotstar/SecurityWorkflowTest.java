package vn.iotstar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.security.CustomUserDetailsService;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.MailService;
import vn.iotstar.service.OtpService;
import vn.iotstar.service.ProductService;

@SpringBootTest
@ActiveProfiles("test")
class SecurityWorkflowTest {
    private static final String TEST_PASSWORD = "TestPassword!24";

    @Autowired private WebApplicationContext context;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private OtpTokenRepository otpTokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthService authService;
    @Autowired private CustomUserDetailsService userDetailsService;
    @Autowired private OtpService otpService;
    @Autowired private ProductService productService;
    @MockitoBean private MailService mailService;
    @MockitoBean private CloudinaryService cloudinaryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void loginPageIsPublicAndRenders() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void formLoginAcceptsUsernameAndEmailAndBuildsCustomPrincipal() throws Exception {
        User user = createUser("ROLE_USER");

        MvcResult usernameLogin = mockMvc.perform(post("/login").with(csrf())
                        .param("username", user.getUsername()).param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername(user.getUsername()))
                .andReturn();
        CustomUserDetails details = (CustomUserDetails) userDetailsService.loadUserByUsername(user.getEmail());
        assertEquals(user.getUsername(), details.getUsername());
        assertEquals(user.getEmail(), details.getEmail());
        assertEquals(user.getFullName(), details.getFullName());
        assertEquals("ROLE_USER", details.getRole());
        mockMvc.perform(get("/").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(details)))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(
                        org.hamcrest.Matchers.containsString(user.getFullName())));

        mockMvc.perform(post("/login").with(csrf())
                        .param("username", user.getEmail()).param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername(user.getUsername()));
    }

    @Test
    void badPasswordAndDisabledAccountCannotAuthenticate() throws Exception {
        User user = createUser("ROLE_USER");
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", user.getUsername()).param("password", "wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login?error*"))
                .andExpect(unauthenticated());

        user.setEnabled(false);
        userRepository.save(user);
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", user.getEmail()).param("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(unauthenticated());
    }

    @Test
    void anonymousIsRedirectedAndRoleRulesSeparateUsersFromAdmins() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login*"));
        mockMvc.perform(get("/dashboard").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(new CustomUserDetails(createUser("ROLE_USER")))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/dashboard").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(new CustomUserDetails(createUser("ROLE_ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(view().name("contents/dashboard"));
    }

    @Test
    void logoutRequiresCsrfAndEndsSession() throws Exception {
        User user = createUser("ROLE_USER");
        CustomUserDetails principal = (CustomUserDetails) userDetailsService.loadUserByUsername(user.getUsername());
        mockMvc.perform(post("/logout").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(principal)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(unauthenticated());

        mockMvc.perform(post("/logout").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrationOtpEnablesUserAndCanOnlyBeUsedOnce() {
        String email = "register-" + UUID.randomUUID() + "@example.com";
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("new-" + UUID.randomUUID());
        dto.setEmail(email);
        dto.setFullName("New Member");
        dto.setPassword(TEST_PASSWORD);
        dto.setConfirmPassword(TEST_PASSWORD);
        authService.register(dto);

        User pending = userRepository.findByEmail(email).orElseThrow();
        assertFalse(pending.isEnabled());
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, pending.getPassword()));
        String code = capturedOtp(email, "đăng ký");
        authService.verifyRegistration(email, code);
        assertTrue(userRepository.findByEmail(email).orElseThrow().isEnabled());
        assertFalse(otpTokenRepository.findTopByEmailAndTypeOrderByCreatedAtDesc(email, OtpType.REGISTER).isPresent());
        assertThrows(IllegalArgumentException.class, () -> authService.verifyRegistration(email, code));
    }

    @Test
    void registrationAndOtpFormsCompleteThePublicMvcFlow() throws Exception {
        String username = "mvc-" + UUID.randomUUID();
        String email = "mvc-" + UUID.randomUUID() + "@example.com";
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", username)
                        .param("email", email)
                        .param("fullName", "MVC Registration")
                        .param("password", TEST_PASSWORD)
                        .param("confirmPassword", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/verify-otp?email=" + email));

        String code = capturedOtp(email, "đăng ký");
        mockMvc.perform(post("/verify-otp").with(csrf())
                        .param("email", email)
                        .param("otp", code)
                        .param("type", "REGISTER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/login?verified=true"));
        assertTrue(userRepository.findByEmail(email).orElseThrow().isEnabled());
    }

    @Test
    void registrationRejectsDuplicateAccountsAndMismatchedPasswords() {
        User existing = createUser("ROLE_USER");
        RegisterDTO duplicate = new RegisterDTO();
        duplicate.setUsername(existing.getUsername());
        duplicate.setEmail("another-" + UUID.randomUUID() + "@example.com");
        duplicate.setFullName("Duplicate");
        duplicate.setPassword(TEST_PASSWORD);
        duplicate.setConfirmPassword(TEST_PASSWORD);
        assertThrows(IllegalArgumentException.class, () -> authService.register(duplicate));

        RegisterDTO mismatch = new RegisterDTO();
        mismatch.setUsername("mismatch-" + UUID.randomUUID());
        mismatch.setEmail("mismatch-" + UUID.randomUUID() + "@example.com");
        mismatch.setFullName("Mismatch");
        mismatch.setPassword(TEST_PASSWORD);
        mismatch.setConfirmPassword("different-password");
        assertThrows(IllegalArgumentException.class, () -> authService.register(mismatch));
    }

    @Test
    void resendOtpInvalidatesThePreviousCode() {
        String email = "resend-" + UUID.randomUUID() + "@example.com";
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("resend-" + UUID.randomUUID());
        dto.setEmail(email);
        dto.setFullName("Resend User");
        dto.setPassword(TEST_PASSWORD);
        dto.setConfirmPassword(TEST_PASSWORD);
        authService.register(dto);
        String firstCode = capturedOtp(email, "đăng ký");
        authService.resendRegistrationOtp(email);
        org.mockito.ArgumentCaptor<String> codes = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(mailService, org.mockito.Mockito.times(2)).sendOtp(eq(email), codes.capture(), eq("đăng ký"));
        String secondCode = codes.getAllValues().get(1);
        assertThrows(IllegalArgumentException.class, () -> authService.verifyRegistration(email, firstCode));
        authService.verifyRegistration(email, secondCode);
        assertTrue(userRepository.findByEmail(email).orElseThrow().isEnabled());
    }

    @Test
    void wrongOtpAttemptsAreLimitedAndExpiredOtpIsRejected() {
        String email = "otp-" + UUID.randomUUID() + "@example.com";
        otpService.issue(email, OtpType.REGISTER);
        String code = capturedOtp(email, "đăng ký");
        String wrongCode = "000000".equals(code) ? "000001" : "000000";
        for (int attempt = 0; attempt < 5; attempt++) {
            assertThrows(IllegalArgumentException.class, () -> otpService.verify(email, OtpType.REGISTER, wrongCode));
        }
        assertThrows(IllegalArgumentException.class, () -> otpService.verify(email, OtpType.REGISTER, code));

        String expiredEmail = "expired-" + UUID.randomUUID() + "@example.com";
        otpService.issue(expiredEmail, OtpType.REGISTER);
        OtpToken token = otpTokenRepository.findTopByEmailAndTypeOrderByCreatedAtDesc(expiredEmail, OtpType.REGISTER).orElseThrow();
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        otpTokenRepository.save(token);
        assertThrows(IllegalArgumentException.class,
                () -> otpService.verify(expiredEmail, OtpType.REGISTER, capturedOtp(expiredEmail, "đăng ký")));
    }

    @Test
    void forgotPasswordOtpUpdatesOnlyTheEncodedPassword() {
        User user = createUser("ROLE_USER");
        authService.requestPasswordReset(user.getEmail());
        String code = capturedOtp(user.getEmail(), "đặt lại mật khẩu");
        authService.resetPassword(user.getEmail(), code, "ChangedPassword!25", "ChangedPassword!25");
        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("ChangedPassword!25", updated.getPassword()));
        assertFalse(passwordEncoder.matches(TEST_PASSWORD, updated.getPassword()));
        assertNotNull(updated.getPassword());
        assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword(user.getEmail(), code, "AnotherPassword!26", "AnotherPassword!26"));
    }

    @Test
    void productCrudSearchPagingAndOwnershipAreEnforced() {
        User owner = createUser("ROLE_USER");
        User stranger = createUser("ROLE_USER");
        ProductDTO dto = new ProductDTO();
        dto.setName("Test item " + UUID.randomUUID());
        dto.setDescription("Searchable description");
        dto.setPrice(new BigDecimal("12.50"));
        ProductDTO created = productService.create(dto, null, owner.getId());

        assertEquals(owner.getId(), created.getUserId());
        assertEquals(1, productService.findAll("Searchable description", 0, 5, null).getTotalElements());
        assertEquals(1, productService.findAll("Searchable", 0, 5, owner.getId()).getTotalElements());
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> productService.findById(created.getId(), stranger.getId(), false));
        assertNotNull(productService.findById(created.getId(), stranger.getId(), true));
        productService.delete(created.getId(), owner.getId(), false);
    }

    @Test
    void productImageUsesCloudinaryAndDeletesTheRemoteAsset() {
        User owner = createUser("ROLE_USER");
        ProductDTO dto = new ProductDTO();
        dto.setName("Uploaded image product");
        dto.setPrice(new BigDecimal("5.00"));
        MockMultipartFile image = new MockMultipartFile("image", "photo.png", "image/png", new byte[] {1, 2, 3});
        when(cloudinaryService.upload(any())).thenReturn(new vn.iotstar.service.CloudinaryUploadResult(
                "https://images.example/product.png", "products/test-image"));

        ProductDTO created = productService.create(dto, image, owner.getId());
        assertEquals("https://images.example/product.png", created.getImageUrl());
        productService.delete(created.getId(), owner.getId(), false);
        verify(cloudinaryService).delete("products/test-image");
    }

    @Test
    void authenticatedProductPageRendersCustomPrincipalAndFallbackImage() throws Exception {
        User owner = createUser("ROLE_USER");
        ProductDTO dto = new ProductDTO();
        dto.setName("Visible fallback product");
        dto.setDescription("No remote image");
        dto.setPrice(new BigDecimal("7.00"));
        productService.create(dto, null, owner.getId());
        CustomUserDetails principal = (CustomUserDetails) userDetailsService.loadUserByUsername(owner.getUsername());

        mockMvc.perform(get("/products").with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString(owner.getFullName())))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("avatar-default.svg")));
    }

    @Test
    void csrfProtectedProductMutationIsRejectedWithoutToken() throws Exception {
        mockMvc.perform(post("/products").param("name", "No token").param("price", "1.00"))
                .andExpect(status().isForbidden());
    }

    private User createUser(String roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = new User();
        user.setUsername("user-" + UUID.randomUUID());
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setFullName("Test Full Name");
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private String capturedOtp(String email, String purpose) {
        org.mockito.ArgumentCaptor<String> otp = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(mailService).sendOtp(eq(email), otp.capture(), eq(purpose));
        return otp.getValue();
    }
}
