package com.example.victorymoments.dto;

import com.example.victorymoments.entity.VisibilityStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    private VisibilityStatus visibilityStatus;

    private Set<String> authorizedViewerIds;

    private List<String> tags;

    private int likeCount;
    private List<String> likedByUsers;
    private int commentCount;
    private int shareCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
}