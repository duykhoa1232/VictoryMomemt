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
        description = "Thông tin phản hồi của yêu cầu kết bạn"
)
public class FriendResponse {

    @Schema(
            description = "ID của yêu cầu kết bạn",
            example = "665c2e2e7b2e8e3a89f6c1a5"
    )
    private String id;

    @Schema(
            description = "ID của người gửi yêu cầu kết bạn",
            example = "665c2e2e7b2e8e3a89f6c111"
    )
    private String requesterId;

    @Schema(
            description = "ID của người nhận yêu cầu kết bạn",
            example = "665c2e2e7b2e8e3a89f6c222"
    )
    private String receiverId;

    @Schema(
            description = "Trạng thái của yêu cầu kết bạn",
            example = "PENDING"
    )
    private String status;

    @Schema(
            description = "Thời gian tạo yêu cầu",
            example = "2024-06-19T10:15:30"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Thời gian cập nhật lần cuối",
            example = "2024-06-19T10:20:45"
    )
    private LocalDateTime updatedAt;

    @Schema(
            description = "Tên người gửi yêu cầu",
            example = "Nguyen Xuan Bao"
    )
    private String requesterName;

    @Schema(
            description = "Email người gửi yêu cầu",
            example = "bao.nguyen@example.com"
    )
    private String requesterEmail;

    @Schema(
            description = "Link ảnh đại diện của người gửi",
            example = "https://example.com/avatars/requester-avatar.png"
    )
    private String requesterAvatar;

    @Schema(
            description = "Tên người nhận lời mời",
            example = "Tran Van A"
    )
    private String receiverName;

    @Schema(
            description = "Email người nhận lời mời",
            example = "tranvana@example.com"
    )
    private String receiverEmail;

    @Schema(
            description = "Link ảnh đại diện của người nhận",
            example = "https://example.com/avatars/receiver-avatar.png"
    )
    private String receiverAvatar;
}
