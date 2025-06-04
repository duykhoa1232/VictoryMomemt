package com.example.victorymoments.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostResponse {
    private String id;
    private String userId;
    private String userEmail;
    private String userName;

    private String content;
    private List<String> imageUrls;
    private List<String> videoUrls;
    private List<String> audioUrls;

    private String location;
    private String privacy;
    private List<String> tags;

    private int likeCount;
    private List<String> likedByUsers;
    private int commentCount;
    private int shareCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
}