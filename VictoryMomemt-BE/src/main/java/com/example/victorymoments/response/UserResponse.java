package com.example.victorymoments.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(
        description = "Thông tin người dùng"
)
public class UserResponse {

    @Schema(
            description = "ID của người dùng",
            example = "user123"
    )
    private String id;

    @Schema(
            description = "Tên hiển thị của người dùng",
            example = "Nguyen Xuan Bao"
    )
    private String name;

    @Schema(
            description = "Email của người dùng",
            example = "bao.nguyen@example.com"
    )
    private String email;

    @Schema(
            description = "URL avatar của người dùng",
            example = "https://example.com/images/avatar.png"
    )
    private String avatarUrl;
}
