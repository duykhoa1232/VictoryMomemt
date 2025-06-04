package com.example.victorymoments.dto;

import com.example.victorymoments.entity.VisibilityStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Data
public class PostRequest {

    private String content;

    @Size(max = 1000, message = "Maximum 1000 images allowed")
    private List<MultipartFile> images;

    @Size(max = 1000, message = "Maximum 1000 video allowed")
    private List<MultipartFile> videos;

    @Size(max = 500, message = "Maximum 500 audio allowed")
    private List<MultipartFile> audios;

    private String location;

    private VisibilityStatus visibilityStatus;

    private Set<String> sharedWithUserIds;

    private List<String> tags;
}