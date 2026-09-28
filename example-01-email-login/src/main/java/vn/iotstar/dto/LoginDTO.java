package vn.iotstar.dto;
import jakarta.validation.constraints.*;
public record LoginDTO(@Email @NotBlank String email, @NotBlank @Size(min=6,max=100) String password) {}
