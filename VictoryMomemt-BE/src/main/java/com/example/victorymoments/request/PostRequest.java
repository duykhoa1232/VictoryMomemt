package com.example.victorymoments.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Payload để tạo hoặc cập nhật bài viết",
        requiredMode = Schema.RequiredMode.REQUIRED
)
public class PostRequest {

    @NotBlank(message = "{post.content.required}")
    @Schema(
            description = "Nội dung bài viết (văn bản)",
            example = "Hôm nay thật tuyệt vời bên bãi biển!"
    )
    private String content;

    @Schema(
            description = "Danh sách file hình ảnh",
            type = "array",
            format = "binary"
    )
    private List<MultipartFile> images;

    @Schema(
            description = "Danh sách file video",
            type = "array",
            format = "binary"
    )
    private List<MultipartFile> videos;

    @Schema(
            description = "Danh sách file âm thanh",
            type = "array",
            format = "binary"
    )
    private List<MultipartFile> audios;

    @Schema(
            description = "Vị trí liên quan đến bài viết",
            example = "Da Nang, Vietnam"
    )
    private String location;

    @Schema(
            description = "Chế độ riêng tư của bài viết",
            example = "PUBLIC",
            allowableValues = {"PUBLIC", "PRIVATE"}
    )
    private String privacy;

    @Schema(
            description = "Danh sách ID người dùng được chia sẻ bài viết (chỉ cho PRIVATE)",
            example = "[\"user123\", \"user456\"]"
    )
    private Set<String> sharedWithUserIds;

    @Schema(
            description = "Danh sách tag của bài viết",
            example = "[\"vacation\", \"beach\"]"
    )
    private List<String> tags;
}
