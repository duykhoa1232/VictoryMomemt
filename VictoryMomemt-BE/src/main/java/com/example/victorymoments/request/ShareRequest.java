package com.example.victorymoments.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        description = "Payload để chia sẻ bài viết gốc",
        requiredMode = Schema.RequiredMode.REQUIRED
)
public class ShareRequest {

    @NotNull(message = "{share.originalPostId.required}")
    @Schema(
            description = "ID của bài post gốc cần chia sẻ",
            example = "66c123abc4567890ef12abcd"
    )
    private String originalPostId;

    @Schema(
            description = "Nội dung người dùng muốn thêm vào bài chia sẻ",
            example = "Bài post này rất hay, mọi người cùng xem nhé!"
    )
    private String content;
}
