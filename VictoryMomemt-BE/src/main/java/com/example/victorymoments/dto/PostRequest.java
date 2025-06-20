package com.example.victorymoments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating or updating a post")
public class PostRequest {

    @Schema(description = "ID of the user creating or updating the post", example = "user123")
    private String userId;

    @Schema(description = "Text content of the post", example = "Today was amazing at the beach!")
    private String content;

    @Size(max = 1000, message = "{post.images.max}")
    @Schema(description = "List of image files to upload", type = "array", format = "binary")
    private List<MultipartFile> images;

    @Size(max = 1000, message = "{post.videos.max}")
    @Schema(description = "List of video files to upload", type = "array", format = "binary")
    private List<MultipartFile> videos;

    @Size(max = 500, message = "{post.audios.max}")
    @Schema(description = "List of audio files to upload", type = "array", format = "binary")
    private List<MultipartFile> audios;

    @Schema(description = "Location associated with the post", example = "Da Nang, Vietnam")
    private String location;

    @Schema(
        description = "Privacy setting of the post",
        example = "PUBLIC",
        allowableValues = {"PUBLIC", "PRIVATE"}
    )
    private String privacy;

    @Schema(description = "List of user IDs with whom the post is shared (for PRIVATE posts)")
    private Set<String> sharedWithUserIds;

    @Schema(description = "List of tags associated with the post", example = "[\"vacation\", \"beach\"]")
    private List<String> tags;
}
