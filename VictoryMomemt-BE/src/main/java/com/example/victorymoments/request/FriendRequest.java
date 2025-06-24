package com.example.victorymoments.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        description = "Payload để gửi yêu cầu kết bạn"
)
public class FriendRequest {

    @Schema(
            description = "ID của người nhận lời mời kết bạn",
            example = "665c2e2e7b2e8e3a89f6c1a5"
    )
    private String receiverId;
}
