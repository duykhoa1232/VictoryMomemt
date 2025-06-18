package com.example.victorymoments.controller;

import com.example.victorymoments.dto.PostRequest;
import com.example.victorymoments.dto.PostResponse;
import com.example.victorymoments.service.PostService;
import com.example.victorymoments.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Posts", description = "Endpoints for managing posts")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "Create a Post", description = "Create a new post with optional media and visibility settings. User must be authenticated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input or validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @ModelAttribute PostRequest request) {
        return new ResponseEntity<>(postService.createPost(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Get All Posts", description = "Retrieve a paginated list of all active posts visible to the current user (public, private based on authorization).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String authenticatedUserEmail = SecurityUtil.getCurrentUsername();
        return ResponseEntity.ok(postService.getAllPosts(pageable, authenticatedUserEmail));
    }

    @Operation(summary = "Get Post by ID", description = "Retrieve a specific post by its ID, considering visibility and user authorization.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found - Post not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Access denied to private post"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Post is inactive")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable String postId) {
        String authenticatedUserEmail = SecurityUtil.getCurrentUsername();

        return ResponseEntity.ok(postService.getPostById(postId, authenticatedUserEmail));
    }


    @Operation(summary = "Get Current User's Posts", description = "Retrieve posts authored by or explicitly shared with the authenticated user, with pagination.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/my-posts")
    public ResponseEntity<Page<PostResponse>> getMyPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String authenticatedUserEmail = SecurityUtil.getCurrentUsername();
        return ResponseEntity.ok(postService.getPostsForCurrentUser(pageable, authenticatedUserEmail));
    }

    @Operation(summary = "Update a Post", description = "Update an existing post. Only the owner can update. Requires authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
            @ApiResponse(responseCode = "404", description = "Not Found - Post not found")
    })
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String postId,
            @Valid @ModelAttribute PostRequest request,
            @RequestParam(value = "deletedMediaUrls", required = false) String deletedMediaUrls) {
        String currentUsername = SecurityUtil.getCurrentUsername();
        return ResponseEntity.ok(postService.updatePost(postId, request, deletedMediaUrls, currentUsername));
    }

    @Operation(summary = "Delete a Post", description = "Soft delete a post by its ID. Only the owner can delete. Requires authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
            @ApiResponse(responseCode = "404", description = "Not Found - Post not found"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Post already inactive")
    })
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable String postId) {
        String currentUsername = SecurityUtil.getCurrentUsername();
        postService.deletePost(postId, currentUsername);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle Post Like", description = "Like or unlike a post. User must be authenticated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like status toggled successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Access denied to private post"),
            @ApiResponse(responseCode = "404", description = "Not Found - Post not found"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Post inactive")
    })
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/{postId}/like")
    public ResponseEntity<PostResponse> toggleLike(@PathVariable String postId) {
        String currentUsername = SecurityUtil.getCurrentUsername();
        return ResponseEntity.ok(postService.toggleLike(postId, currentUsername));
    }

}