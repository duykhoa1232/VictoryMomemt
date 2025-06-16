//package com.example.victorymoments.controller;
//
//import com.example.victorymoments.dto.CommentRequest;
//import com.example.victorymoments.dto.CommentResponse;
//import com.example.victorymoments.service.CommentService;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//
//import java.util.List;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//
//
//
//@Tag(name = "Comments", description = "Endpoints for managing comments on posts")
//@RestController
//@RequestMapping("/api/posts/{postId}/comments")
//@RequiredArgsConstructor
//public class CommentController {
//
//    private final CommentService commentService;
//
//    @Operation(summary = "Create a Comment", description = "Add a new comment to a post")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Comment created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
//            @ApiResponse(responseCode = "401", description = "Unauthorized")
//    })
//    @PostMapping
//    public ResponseEntity<CommentResponse> createComment(
//            @PathVariable String postId,
//            @Valid @RequestBody CommentRequest request,
//            @AuthenticationPrincipal UserDetails currentUser) {
//        return ResponseEntity.ok(commentService.createComment(postId, request, currentUser.getUsername()));
//    }
//
//    @Operation(summary = "Get Comments by Post", description = "Retrieve all comments for a specific post")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class)))
//    })
//    @GetMapping
//    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable String postId) {
//        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
//    }
//
//    @Operation(summary = "Update a Comment", description = "Update an existing comment")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Comment updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
//            @ApiResponse(responseCode = "401", description = "Unauthorized")
//    })
//    @PutMapping("/{commentId}")
//    public ResponseEntity<CommentResponse> updateComment(
//            @PathVariable String postId,
//            @PathVariable String commentId,
//            @Valid @RequestBody CommentRequest request,
//            @AuthenticationPrincipal UserDetails currentUser) {
//        return ResponseEntity.ok(commentService.updateComment(commentId, request, currentUser.getUsername()));
//    }
//
//    @Operation(summary = "Delete a Comment", description = "Delete a specific comment")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized")
//    })
//    @DeleteMapping("/{commentId}")
//    public ResponseEntity<Void> deleteComment(
//            @PathVariable String postId,
//            @PathVariable String commentId,
//            @AuthenticationPrincipal UserDetails currentUser) {
//        commentService.deleteComment(commentId, currentUser.getUsername());
//        return ResponseEntity.noContent().build();
//    }
//}


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
@RequestMapping("/api") // Base path cho tất cả API
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // API để tạo bình luận cho một bài đăng
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
        @PathVariable String postId,
        @Valid @RequestBody CommentRequest request,
        @RequestParam String userEmail) {
        CommentResponse newComment = commentService.createComment(postId, request, userEmail);
        return new ResponseEntity<>(newComment, HttpStatus.CREATED);
    }

    // API để lấy tất cả bình luận cho một bài đăng (đã được cấu trúc cây ở backend)
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable String postId) {
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    // API để cập nhật bình luận
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
        @PathVariable String commentId,
        @Valid @RequestBody CommentRequest request,
        @RequestParam String userEmail) {
        CommentResponse updatedComment = commentService.updateComment(commentId, request, userEmail);
        return ResponseEntity.ok(updatedComment);
    }

    // API để xóa bình luận
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
        @PathVariable String commentId,
        @RequestParam String userEmail) {
        commentService.deleteComment(commentId, userEmail);
        return ResponseEntity.noContent().build();
    }
}
