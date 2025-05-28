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

    private String userId;      // ID của user tạo post (lấy từ auth context)
    private String userEmail;   // Email của user (lấy từ auth context)
    private String userName;    // Tên user (lấy từ auth context)

    private String content;

    // Media files
    private List<String> imageUrls;
    private List<String> videoUrls;
    private List<String> audioUrls;

    // Additional fields (giống Facebook)
    private String location;    // Vị trí check-in
    private String privacy;     // public, friends, private
    private List<String> tags;  // Tag bạn bè

    // Engagement
    private int likeCount = 0;
    private int commentCount = 0;
    private int shareCount = 0;

    // Timestamps
    @CreatedDate // Sẽ tự động điền khi tạo mới
    private LocalDateTime createdAt;

    @LastModifiedDate // Sẽ tự động cập nhật khi chỉnh sửa
    private LocalDateTime updatedAt;

    // Status
    private boolean isActive = true;
}