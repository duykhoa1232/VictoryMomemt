package com.example.victorymoments.response;

import com.example.victorymoments.entity.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Thông tin phản hồi của media đã được xử lý"
)
public class MediaResponse {

    @Schema(
            description = "ID của media",
            example = "665c2e2e7b2e8e3a89f6c1a5"
    )
    private String id;

    @Schema(
            description = "Đường dẫn của media",
            example = "https://example.com/media/image123.png"
    )
    private String path;

    @Schema(
            description = "Loại media (IMAGE, VIDEO, AUDIO)",
            example = "IMAGE"
    )
    private MediaType type;

    @Schema(
            description = "ID của post sở hữu media này",
            example = "665c2e2e7b2e8e3a89f6c1a5"
    )
    private String postId;
}
