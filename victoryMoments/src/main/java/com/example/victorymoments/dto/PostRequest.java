package com.example.victorymoments.dto;

import com.example.victorymoments.entity.VisibilityStatus; // IMPORTANT: Import VisibilityStatus enum
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank; // For non-blank string
import jakarta.validation.constraints.NotNull;  // For non-null object (like enum)
import jakarta.validation.constraints.Size;    // For size constraints
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating or updating a post")
public class PostRequest {


    @NotBlank(message = "{post.content.required}")
    @Size(max = 2000, message = "{post.content.max_length}")
    @Schema(description = "Text content of the post", example = "Today was amazing at the beach!")
    private String content;

    @Size(max = 10, message = "{post.images.max}")
    @Schema(description = "List of image files to upload", type = "array", format = "binary")
    private List<MultipartFile> images;

    @Size(max = 5, message = "{post.videos.max}")
    @Schema(description = "List of video files to upload", type = "array", format = "binary")
    private List<MultipartFile> videos;

    @Size(max = 3, message = "{post.audios.max}")
    @Schema(description = "List of audio files to upload", type = "array", format = "binary")
    private List<MultipartFile> audios;

    @Schema(description = "Location associated with the post", example = "Da Nang, Vietnam")
    private String location;

    @NotNull(message = "{post.privacy.required}")
    @Schema(
            description = "Privacy setting of the post (PUBLIC, PRIVATE, FRIENDS)",
            example = "PUBLIC",
            allowableValues = {"PUBLIC", "PRIVATE", "FRIENDS"}
    )
    private VisibilityStatus visibilityStatus;

    @Schema(description = "List of user IDs with whom the post is explicitly shared (for PRIVATE posts)")
    private List<String> authorizedViewerIds;

    @Size(max = 10, message = "{post.tags.max}")
    @Schema(description = "List of tags associated with the post", example = "[\"vacation\", \"beach\"]")
    private List<String> tags;
}