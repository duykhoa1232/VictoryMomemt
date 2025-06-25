package com.example.victorymoments.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        description = "Thông tin phản hồi sau khi chia sẻ bài post"
)
public class ShareResponse {

    @Schema(
            description = "ID của bản ghi chia sẻ",
            example = "abc123xyz456"
    )
    private String id;

    @Schema(
            description = "ID của bài post gốc được chia sẻ",
            example = "66c123abc4567890ef12abcd"
    )
    private String originalPostId;

    @Schema(
            description = "Thông tin người chia sẻ",
            implementation = UserResponse.class
    )
    private UserResponse sharedBy;

    @Schema(
            description = "Nội dung của người chia sẻ thêm vào",
            example = "Bài post này rất hay!"
    )
    private String content;

    @Schema(
            description = "Thông tin bài post gốc",
            implementation = PostResponse.class
    )
    private PostResponse originalPost;

    @Schema(
            description = "Thời gian bài post chia sẻ được tạo"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Thời gian bài post chia sẻ được chỉnh sửa gần nhất"
    )
    private LocalDateTime updatedAt;

    @Schema(
            description = "Trạng thái hoạt động của bản ghi chia sẻ"
    )
    private boolean isActive;
}
