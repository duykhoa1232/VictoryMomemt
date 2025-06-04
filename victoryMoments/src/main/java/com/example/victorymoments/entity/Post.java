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
import java.util.ArrayList;
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
    private List<String> videoUrls; // no
    private List<String> audioUrls; // no

    private String location;
    private String privacy; //no
    private List<String> tags; //no

    private int likeCount = 0;
    private List<String> likedByUsers = new ArrayList<>();
    private int commentCount = 0;
    private int shareCount = 0;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private boolean isActive = true;
}