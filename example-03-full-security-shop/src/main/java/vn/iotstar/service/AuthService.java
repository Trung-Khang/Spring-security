package vn.iotstar.service;

import vn.iotstar.dto.RegisterDTO;

public interface AuthService {
    void register(RegisterDTO dto);
    void verifyRegistration(String email, String otp);
    void resendRegistrationOtp(String email);
    void requestPasswordReset(String email);
    void resetPassword(String email, String otp, String newPassword, String confirmPassword);
}
