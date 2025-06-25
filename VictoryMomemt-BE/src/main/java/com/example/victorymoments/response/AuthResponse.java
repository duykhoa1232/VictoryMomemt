package com.example.victorymoments.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Payload for authentication response containing JWT token"
)
public class AuthResponse {

    @Schema(
            description = "JWT token được cấp sau khi đăng nhập hoặc đăng ký thành công",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String token;


    @Schema(
            description = "JWT refresh token được cập nhật lại sau khi hết hạn token",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String refresh;
}
