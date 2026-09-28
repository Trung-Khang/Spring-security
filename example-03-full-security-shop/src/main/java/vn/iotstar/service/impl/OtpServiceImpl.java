package vn.iotstar.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.service.MailService;
import vn.iotstar.service.OtpService;

@Service
@Transactional
public class OtpServiceImpl implements OtpService {
    private static final int MAX_ATTEMPTS = 5;
    private static final int OTP_MINUTES = 5;
    private final OtpTokenRepository repository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public OtpServiceImpl(OtpTokenRepository repository, MailService mailService, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void issue(String email, OtpType type) {
        repository.deleteByEmailAndType(email, type);
        String code = String.format("%06d", random.nextInt(1_000_000));
        OtpToken token = new OtpToken();
        token.setEmail(email);
        token.setOtpCode(passwordEncoder.encode(code));
        token.setType(type);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_MINUTES));
        token.setAttempts(0);
        token.setVerified(false);
        repository.save(token);
        mailService.sendOtp(email, code, type == OtpType.REGISTER ? "đăng ký" : "đặt lại mật khẩu");
    }

    @Override
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void verify(String email, OtpType type, String code) {
        OtpToken token = currentToken(email, type);
        validateUsable(token);
        if (token.isVerified() && passwordEncoder.matches(code, token.getOtpCode())) {
            return;
        }
        if (!passwordEncoder.matches(code, token.getOtpCode())) {
            token.setAttempts(token.getAttempts() + 1);
            throw new IllegalArgumentException("OTP không hợp lệ hoặc đã hết hạn.");
        }
        token.setVerified(true);
    }

    @Override
    public void consumeVerified(String email, OtpType type, String code) {
        OtpToken token = currentToken(email, type);
        validateUsable(token);
        if (!token.isVerified() || !passwordEncoder.matches(code, token.getOtpCode())) {
            throw new IllegalArgumentException("Hãy xác nhận OTP trước khi tiếp tục.");
        }
        repository.delete(token);
    }

    private OtpToken currentToken(String email, OtpType type) {
        return repository.findTopByEmailAndTypeOrderByCreatedAtDesc(email, type)
                .orElseThrow(() -> new IllegalArgumentException("OTP không hợp lệ hoặc đã hết hạn."));
    }

    private void validateUsable(OtpToken token) {
        if (token.getAttempts() >= MAX_ATTEMPTS
                || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP không hợp lệ hoặc đã hết hạn.");
        }
    }
}
