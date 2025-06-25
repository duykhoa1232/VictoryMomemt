package com.example.victorymoments.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        description = "Phản hồi thông tin hồ sơ của người dùng"
)
public class ProfileResponse {

    @Schema(
            description = "ID của người dùng",
            example = "665c2e2e7b2e8e3a89f6c1a5"
    )
    private String id;

    @Schema(
            description = "Tên của người dùng",
            example = "Nguyen Xuan Bao"
    )
    private String name;

    @Schema(
            description = "Email của người dùng",
            example = "bao.nguyen@example.com"
    )
    private String email;

    @Schema(
            description = "Số điện thoại của người dùng",
            example = "+84 912345678"
    )
    private String phoneNumber;

    @Schema(
            description = "Đường dẫn URL ảnh đại diện của người dùng",
            example = "https://my-cdn.com/images/avatar/665c2e2e7b2e8e3a89f6c1a5.png"
    )
    private String avatarUrl;

    @Schema(
            description = "Mô tả ngắn (bio) của người dùng",
            example = "Passionate software developer who loves to travel and write code."
    )
    private String bio;

    @Schema(
            description = "Thời gian tài khoản được tạo"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Thời gian thông tin hồ sơ được cập nhật gần nhất"
    )
    private LocalDateTime updatedAt;

    @Schema(
            description = "Tài khoản có đang hoạt động không",
            example = "true"
    )
    private boolean enabled;

    @Schema(
            description = "Danh sách bài đăng của người dùng",
            implementation = PostResponse.class
    )
    private Page<PostResponse> userPosts;
}
