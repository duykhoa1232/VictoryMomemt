//package com.example.victorymoments.service;
//
//import com.example.victorymoments.dto.CommentRequest;
//import com.example.victorymoments.dto.CommentResponse;
//
//import java.util.List;
//
//public interface CommentService {
//    CommentResponse createComment(String postId, CommentRequest request, String userEmail);
//    List<CommentResponse> getCommentsByPostId(String postId);
//    CommentResponse updateComment(String commentId, CommentRequest request, String userEmail);
//    void deleteComment(String commentId, String userEmail);
//}


package com.example.victorymoments.service;

import com.example.victorymoments.dto.CommentRequest;
import com.example.victorymoments.dto.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(String postId, CommentRequest request, String userEmail);
    List<CommentResponse> getCommentsByPostId(String postId);
    CommentResponse updateComment(String commentId, CommentRequest request, String userEmail);
    void deleteComment(String commentId, String userEmail);
}
