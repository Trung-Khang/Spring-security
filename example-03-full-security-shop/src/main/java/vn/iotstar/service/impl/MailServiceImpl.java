package vn.iotstar.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.iotstar.service.MailService;

@Service
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;
    private final String from;

    public MailServiceImpl(JavaMailSender mailSender, @Value("${MAIL_USERNAME:}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendOtp(String recipient, String otp, String purpose) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (!from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(recipient);
        message.setSubject("Mã OTP " + purpose + " - IOTSTAR Shop");
        message.setText("Mã xác nhận của bạn là " + otp
                + ". Mã có hiệu lực trong 5 phút và chỉ sử dụng một lần.");
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new IllegalStateException("Không thể gửi email. Hãy kiểm tra cấu hình Mail.", exception);
        }
    }
}
