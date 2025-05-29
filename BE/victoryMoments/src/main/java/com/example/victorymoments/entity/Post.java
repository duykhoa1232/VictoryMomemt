package com.example.victorymoments.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
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

    // Engagement
    private int likeCount = 0;
    private int commentCount = 0;
    private int shareCount = 0;

    // Timestamps
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Status
    private boolean isActive = true;
}