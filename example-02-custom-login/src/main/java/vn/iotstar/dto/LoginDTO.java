package vn.iotstar.dto; import jakarta.validation.constraints.NotBlank; public record LoginDTO(@NotBlank(message="Username hoặc email không được để trống") String login,@NotBlank String password){}
