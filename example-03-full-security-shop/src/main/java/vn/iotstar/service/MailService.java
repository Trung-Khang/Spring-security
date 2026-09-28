package vn.iotstar.service;

public interface MailService {
    void sendOtp(String recipient, String otp, String purpose);
}
