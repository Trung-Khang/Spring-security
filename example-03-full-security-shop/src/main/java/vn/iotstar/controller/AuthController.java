package vn.iotstar.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.dto.OtpVerifyDTO;
import vn.iotstar.service.AuthService;

@Controller
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login() { return "auth/login"; }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterDTO registerDTO, BindingResult result, Model model) {
        if (result.hasErrors()) return "auth/register";
        try {
            authService.register(registerDTO);
            return "redirect:/verify-otp?email=" + registerDTO.getEmail();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            model.addAttribute("error", exception.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyForm(@RequestParam(required = false) String email, Model model) {
        OtpVerifyDTO dto = new OtpVerifyDTO();
        dto.setEmail(email);
        dto.setType("REGISTER");
        model.addAttribute("otpVerifyDTO", dto);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verify(@Valid @ModelAttribute OtpVerifyDTO otpVerifyDTO, BindingResult result, Model model) {
        if (result.hasErrors()) return "auth/verify-otp";
        try {
            authService.verifyRegistration(otpVerifyDTO.getEmail(), otpVerifyDTO.getOtp());
            return "redirect:/login?verified=true";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            return "auth/verify-otp";
        }
    }

    @PostMapping("/resend-register-otp")
    public String resend(@RequestParam String email, Model model) {
        try {
            authService.resendRegistrationOtp(email);
            model.addAttribute("success", "OTP mới đã được gửi nếu tài khoản đang chờ xác nhận.");
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
        }
        OtpVerifyDTO dto = new OtpVerifyDTO();
        dto.setEmail(email);
        dto.setType("REGISTER");
        model.addAttribute("otpVerifyDTO", dto);
        return "auth/verify-otp";
    }

    @GetMapping("/forgot-password")
    public String forgotForm(Model model) {
        model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgot(@Valid @ModelAttribute ForgotPasswordDTO forgotPasswordDTO,
            BindingResult result, Model model) {
        if (result.hasErrors()) return "auth/forgot-password";
        try {
            authService.requestPasswordReset(forgotPasswordDTO.getEmail());
            model.addAttribute("success", "Nếu tài khoản tồn tại, mã OTP sẽ được gửi đến email.");
            model.addAttribute("email", forgotPasswordDTO.getEmail());
            model.addAttribute("resetPasswordDTO", new ResetPasswordDTO());
            return "auth/reset-password";
        } catch (IllegalStateException exception) {
            model.addAttribute("error", exception.getMessage());
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetForm(@RequestParam(required = false) String email, Model model) {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setEmail(email);
        model.addAttribute("resetPasswordDTO", dto);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String reset(@Valid @ModelAttribute ResetPasswordDTO resetPasswordDTO,
            BindingResult result, Model model) {
        if (result.hasErrors()) return "auth/reset-password";
        try {
            authService.resetPassword(resetPasswordDTO.getEmail(), resetPasswordDTO.getOtp(),
                    resetPasswordDTO.getNewPassword(), resetPasswordDTO.getConfirmPassword());
            return "redirect:/login?reset=true";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            return "auth/reset-password";
        }
    }
}
