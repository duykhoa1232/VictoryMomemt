//package com.example.victorymoments.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class CommentResponse {
//    private String id;
//    private String postId;
//    private String userId;
//    private String userEmail;
//    private String userName;
//    private String content;
//    private String parentCommentId;
//    private int replyCount;
//
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//    private boolean isActive;
//}



package com.example.victorymoments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List; // Import List

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private String id;
    private String postId;
    private String userId;
    private String userEmail;
    private String userName;
    private String userAvatar; // Nếu có trường này trong entity Comment
    private String content;
    private String parentCommentId;
    private int replyCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private List<CommentResponse> replies; // THÊM TRƯỜNG NÀY
}
