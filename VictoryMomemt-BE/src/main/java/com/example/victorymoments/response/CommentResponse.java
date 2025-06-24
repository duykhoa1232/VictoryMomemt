package com.example.victorymoments.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(
        description = "Comment response object containing comment details"
)
public class CommentResponse {

    @Schema(
            description = "Comment ID",
            example = "665c123abc456def789ghi"
    )
    private String id;

    @Schema(
            description = "ID của bài post",
            example = "664c123abc456def789ghi"
    )
    private String postId;

    @Schema(
            description = "ID của người dùng đã comment",
            example = "663c123abc456def789ghi"
    )
    private String userId;

    @Schema(
            description = "Email của người dùng đã comment",
            example = "user@example.com"
    )
    private String userEmail;

    @Schema(
            description = "Tên người dùng đã comment",
            example = "John Doe"
    )
    private String userName;

    @Schema(
            description = "Avatar URL của người dùng",
            example = "https://example.com/avatar/user1.png"
    )
    private String userAvatar;

    @Schema(
            description = "Nội dung comment",
            example = "Bài viết này rất hay, mình rất thích!"
    )
    private String content;

    @Schema(
            description = "ID của comment cha (nếu là reply)",
            example = "null"
    )
    private String parentCommentId;

    @Schema(
            description = "Số reply của comment",
            example = "3"
    )
    private int replyCount;

    @Schema(
            description = "Thời gian comment được tạo",
            example = "2025-06-23T14:25:36"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Thời gian comment được chỉnh sửa gần nhất",
            example = "2025-06-23T14:30:10"
    )
    private LocalDateTime updatedAt;

    @Schema(
            description = "Comment còn hoạt động không",
            example = "true"
    )
    private boolean isActive;

    @Schema(
            description = "Danh sách reply comments (nếu cần trả về)",
            example = "[]"
    )
    private List<CommentResponse> replies;
}
