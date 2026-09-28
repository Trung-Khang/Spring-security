package vn.iotstar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordDTO {
    @NotBlank @Email private String email;
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
