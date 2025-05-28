package com.example.victorymoments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PostRequest {
    // userId, userEmail, userName không được gửi từ frontend,
    // mà sẽ được lấy từ SecurityContextHolder ở backend.

    @NotBlank(message = "Content cannot be empty")
    @Size(min = 1, max = 2000, message = "Content must be between 1 and 2000 characters")
    private String content;

    @Size(max = 5, message = "Maximum 5 images allowed") // Giới hạn số lượng file
    private List<MultipartFile> images;

    @Size(max = 1, message = "Maximum 1 video allowed")
    private List<MultipartFile> videos;

    @Size(max = 1, message = "Maximum 1 audio allowed")
    private List<MultipartFile> audios;

    private String location;

    @NotBlank(message = "Privacy must be specified (public, friends, or private)")
    private String privacy; // Ví dụ: "public", "friends", "private"

    private List<String> tags; // Danh sách các tags
}