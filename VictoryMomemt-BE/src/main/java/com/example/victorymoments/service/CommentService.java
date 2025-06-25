
package com.example.victorymoments.service;

import com.example.victorymoments.request.CommentRequest;
import com.example.victorymoments.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(String postId, CommentRequest request, String userEmail);
    Page<CommentResponse> getCommentsByPostId(String postId, Pageable pageable);
    CommentResponse updateComment(String commentId, CommentRequest request, String userEmail);
    void deleteComment(String commentId, String userEmail);
}
