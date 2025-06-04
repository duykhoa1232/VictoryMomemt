package com.example.victorymoments.controller;

import com.example.victorymoments.dto.CommentRequest;
import com.example.victorymoments.dto.CommentResponse;
import com.example.victorymoments.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable String postId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(commentService.createComment(postId, request, currentUser.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(
            @PathVariable String postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable String postId,
            @PathVariable String commentId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(commentService.updateComment(commentId, request, currentUser.getUsername()));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String postId,
            @PathVariable String commentId,
            @AuthenticationPrincipal UserDetails currentUser) {
        commentService.deleteComment(commentId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }
}
