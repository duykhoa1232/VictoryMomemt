package com.example.victorymoments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PostRequest {

    @NotBlank(message = "Content cannot be empty")
    @Size(min = 1, max = 5000, message = "Content must be between 1 and 5000 characters")
    private String content;

    @Size(max = 1000, message = "Maximum 1000 images allowed")
    private List<MultipartFile> images;

    @Size(max = 1000, message = "Maximum 1000 video allowed")
    private List<MultipartFile> videos;

    @Size(max = 500, message = "Maximum 500 audio allowed")
    private List<MultipartFile> audios;

    private String location;

    @NotBlank(message = "Privacy must be specified (public, friends, or private)")
    private String privacy; // Ví dụ: "public", "friends", "private"

    private List<String> tags; // Danh sách các tags
}