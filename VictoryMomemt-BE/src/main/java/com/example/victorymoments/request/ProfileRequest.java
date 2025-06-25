package com.example.victorymoments.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        description = "Payload để chỉnh sửa thông tin hồ sơ của người dùng",
        requiredMode = Schema.RequiredMode.REQUIRED
)
public class ProfileRequest {

    @Schema(
            description = "Tên người dùng",
            example = "Nguyen Xuan Bao"
    )
    private String name;

    @NotBlank(message = "{profile.phone.required}")
    @Pattern(
            regexp = "^[0-9+\\-\\s()]{10,15}$",
            message = "{profile.phone.invalid}"
    )
    @Schema(
            description = "Số điện thoại của người dùng",
            example = "+84 912345678"
    )
    private String phoneNumber;

    @Schema(
            description = "Mô tả ngắn (bio) về bản thân người dùng",
            example = "Passionate software developer who loves to travel and write code."
    )
    private String bio;
}
