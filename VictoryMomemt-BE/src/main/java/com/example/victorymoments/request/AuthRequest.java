package com.example.victorymoments.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Payload for authentication request (email and password)"
)
public class AuthRequest {

    @NotBlank(message = "{auth.email.required}")
    @Size(min = 5, max = 100, message = "{auth.email.size}")
    @Schema(
            description = "Email đăng nhập của người dùng",
            example = "user@example.com"
    )
    private String email;

    @NotBlank(message = "{auth.password.required}")
    @Size(min = 8, message = "{auth.password.size}")
    @Schema(
            description = "Mật khẩu đăng nhập của người dùng",
            example = "P@ssw0rd123"
    )
    private String password;
}
