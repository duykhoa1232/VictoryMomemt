package com.example.victorymoments.request;

import com.example.victorymoments.entity.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Payload để thao tác media của post"
)
public class MediaRequest {

    @Schema(
            description = "ID của bài post mà media này thuộc về",
            example = "665c2e2e7b2e8e3a89f6c1a5"
    )
    private String postId;

    @NotNull
    @Schema(
            description = "Loại media (IMAGE, VIDEO, AUDIO)",
            example = "IMAGE",
            allowableValues = {"IMAGE", "VIDEO", "AUDIO"}
    )
    private MediaType type;
}
