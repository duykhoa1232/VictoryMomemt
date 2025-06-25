package com.example.victorymoments.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Comment request payload for creating or updating a comment"
)
public class CommentRequest {

    @Schema(
            description = "ID của comment cha (nếu reply vào comment khác)",
            example = "665c123abc456def789ghi"
    )
    private String parentCommentId;

    @NotBlank(message = "Comment content is required")
    @Schema(
            description = "Nội dung comment",
            example = "Bài viết này rất hay, mình rất thích!"
    )
    private String content;
}
