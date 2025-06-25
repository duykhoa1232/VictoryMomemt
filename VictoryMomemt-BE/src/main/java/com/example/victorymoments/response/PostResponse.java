package com.example.victorymoments.response;

import com.example.victorymoments.entity.VisibilityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@Schema(
        description = "Phản hồi thông tin bài viết sau khi xử lý"
)
public class PostResponse {

    @Schema(
            description = "ID bài viết",
            example = "665c2e2e7b2e8e3a89f6c1a5"
    )
    private String id;

    @Schema(
            description = "Thông tin người tạo bài viết"
    )
    private UserResponse author;

    @Schema(
            description = "Nội dung bài viết",
            example = "Hôm nay thật tuyệt vời bên bãi biển!"
    )
    private String content;

    @Schema(
            description = "Danh sách URL hình ảnh đính kèm"
    )
    private List<String> imageUrls;

    @Schema(
            description = "Danh sách URL video đính kèm"
    )
    private List<String> videoUrls;

    @Schema(
            description = "Danh sách URL audio đính kèm"
    )
    private List<String> audioUrls;

    @Schema(
            description = "Vị trí liên quan đến bài viết",
            example = "Da Nang, Vietnam"
    )
    private String location;

    @Schema(
            description = "Chế độ riêng tư của bài viết",
            example = "PUBLIC"
    )
    private VisibilityStatus visibilityStatus;

    @Schema(
            description = "Danh sách ID người dùng được phép xem bài viết nếu PRIVATE",
            example = "[\"user123\", \"user456\"]"
    )
    private Set<String> authorizedViewerIds;

    @Schema(
            description = "Danh sách tag",
            example = "[\"vacation\", \"beach\"]"
    )
    private List<String> tags;

    @Schema(
            description = "Số lượt thích",
            example = "10"
    )
    private int likeCount;

    @Schema(
            description = "Danh sách ID người dùng đã thích bài viết",
            example = "[\"user123\", \"user456\"]"
    )
    private List<String> likedByUsers;

    @Schema(
            description = "Số bình luận",
            example = "5"
    )
    private int commentCount;

    @Schema(
            description = "Số lượt chia sẻ",
            example = "3"
    )
    private int shareCount;

    @Schema(
            description = "Thời gian tạo bài viết"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Thời gian cập nhật bài viết gần nhất"
    )
    private LocalDateTime updatedAt;

    @Schema(
            description = "Bài viết có đang hoạt động không"
    )
    private boolean isActive;

    @Schema(
            description = "Người dùng hiện tại đã thích bài viết chưa"
    )
    private boolean isLikedByCurrentUser;

    @Schema(
            description = "Người dùng hiện tại có phải chủ bài viết không"
    )
    private boolean isOwnedByCurrentUser;

    @Schema(
            description = "Danh sách các bình luận hàng đầu"
    )
    private List<CommentResponse> comments;
}
