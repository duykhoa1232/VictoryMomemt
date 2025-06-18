package com.example.victorymoments.controller;

import com.example.victorymoments.dto.CommentRequest;
import com.example.victorymoments.dto.CommentResponse;
import com.example.victorymoments.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;


    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable String postId,
            @Valid @RequestBody CommentRequest request,
            @RequestParam String userEmail) {
        CommentResponse newComment = commentService.createComment(postId, request, userEmail);
        return new ResponseEntity<>(newComment, HttpStatus.CREATED);
    }


    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable String postId) {
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable String commentId,
            @Valid @RequestBody CommentRequest request,
            @RequestParam String userEmail) {
        CommentResponse updatedComment = commentService.updateComment(commentId, request, userEmail);
        return ResponseEntity.ok(updatedComment);
    }


    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String commentId,
            @RequestParam String userEmail) {
        commentService.deleteComment(commentId, userEmail);
        return ResponseEntity.noContent().build();
    }
}
