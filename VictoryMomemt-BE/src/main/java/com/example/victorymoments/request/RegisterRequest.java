package com.example.victorymoments.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Payload để đăng ký tài khoản mới",
        requiredMode = Schema.RequiredMode.REQUIRED
)
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Schema(
            description = "Tên của người dùng",
            example = "Nguyen Xuan Bao"
    )
    private String name;

    @NotBlank(message = "{email.invalid}")
    @Email(message = "{email.invalid}")
    @Schema(
            description = "Email của người dùng",
            example = "bao.nguyen@example.com"
    )
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9+\\-\\s()]{10,15}$",
            message = "Invalid phone number format"
    )
    @Schema(
            description = "Số điện thoại của người dùng",
            example = "+84 912345678"
    )
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    @Schema(
            description = "Mật khẩu của người dùng. Yêu cầu ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt",
            example = "P@ssw0rd123"
    )
    private String password;
}
