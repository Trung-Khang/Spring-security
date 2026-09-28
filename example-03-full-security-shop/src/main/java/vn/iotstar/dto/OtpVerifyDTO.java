package vn.iotstar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OtpVerifyDTO {
    @NotBlank @Email private String email;
    @NotBlank @Pattern(regexp = "\\d{6}") private String otp;
    @NotBlank private String type;
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
