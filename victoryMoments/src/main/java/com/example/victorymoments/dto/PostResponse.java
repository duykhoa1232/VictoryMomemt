package com.example.victorymoments.dto;

import com.example.victorymoments.entity.VisibilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private String id;
    private UserResponse author;
    private String content;
    private List<String> imageUrls;
    private List<String> videoUrls;
    private List<String> audioUrls;
    private String location;
    private VisibilityStatus visibilityStatus;

    private List<String> authorizedViewerIds;

    private List<String> tags;
    private int likeCount;
    private List<String> likedByUsers;
    private int commentCount;
    private int shareCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private boolean isLikedByCurrentUser;

    private boolean isOwnedByCurrentUser;

    private boolean isSharedPost = false;
    private String originalShareId;
    private String sharedByUserId;
    private String sharedByUserName;
    private String sharedContent;
}