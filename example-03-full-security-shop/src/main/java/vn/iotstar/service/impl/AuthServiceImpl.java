package vn.iotstar.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.entity.OtpType;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.OtpService;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, OtpService otpService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    @Override
    public void register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }
        if (userRepository.existsByUsername(dto.getUsername()) || userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Username hoặc email đã được sử dụng.");
        }
        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Chưa khởi tạo ROLE_USER."));
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEnabled(false);
        user.setRole(role);
        userRepository.save(user);
        otpService.issue(user.getEmail(), OtpType.REGISTER);
    }

    @Override
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void verifyRegistration(String email, String otp) {
        otpService.verify(email, OtpType.REGISTER, otp);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại."));
        user.setEnabled(true);
        otpService.consumeVerified(email, OtpType.REGISTER, otp);
    }

    @Override
    public void resendRegistrationOtp(String email) {
        User user = userRepository.findByEmail(email)
                .filter(account -> !account.isEnabled())
                .orElseThrow(() -> new IllegalArgumentException("Không thể gửi lại OTP cho tài khoản này."));
        otpService.issue(user.getEmail(), OtpType.REGISTER);
    }

    @Override
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> otpService.issue(user.getEmail(), OtpType.RESET_PASSWORD));
    }

    @Override
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void resetPassword(String email, String otp, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }
        otpService.verify(email, OtpType.RESET_PASSWORD, otp);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("OTP không hợp lệ hoặc đã hết hạn."));
        user.setPassword(passwordEncoder.encode(newPassword));
        otpService.consumeVerified(email, OtpType.RESET_PASSWORD, otp);
    }
}
