package com.example.victorymoments.controller;

import com.example.victorymoments.request.CommentRequest;
import com.example.victorymoments.response.CommentResponse;
import com.example.victorymoments.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"https://shark-calm-externally.ngrok-free.app", "http://localhost:4200"})
@RequiredArgsConstructor
@Tag(name = "Comment API", description = "Endpoints for creating, reading, updating, and deleting comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Create a new comment on a post")
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable String postId,
            @Valid @RequestBody CommentRequest request,
            @RequestParam String userEmail) {
        CommentResponse newComment = commentService.createComment(postId, request, userEmail);
        return new ResponseEntity<>(newComment, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all comments by post ID with pagination")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> getCommentsByPostId(
            @PathVariable String postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<CommentResponse> comments = commentService.getCommentsByPostId(postId, PageRequest.of(page, size));
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "Update a specific comment")
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable String commentId,
            @Valid @RequestBody CommentRequest request,
            @RequestParam String userEmail) {
        CommentResponse updatedComment = commentService.updateComment(commentId, request, userEmail);
        return ResponseEntity.ok(updatedComment);
    }

    @Operation(summary = "Delete a comment by ID")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String commentId,
            @RequestParam String userEmail) {
        commentService.deleteComment(commentId, userEmail);
        return ResponseEntity.noContent().build();
    }
}
