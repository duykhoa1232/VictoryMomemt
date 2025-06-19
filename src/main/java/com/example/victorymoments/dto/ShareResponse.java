package com.example.victorymoments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareResponse {
    private String id;
    private String originalPostId;
    private UserResponse sharedBy;
    private String content;

    private PostResponse originalPost;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
}
