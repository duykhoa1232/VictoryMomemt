package com.example.victorymoments.controller;

import com.example.victorymoments.dto.PostRequest;
import com.example.victorymoments.dto.PostResponse;
import com.example.victorymoments.dto.UserResponse;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.service.PostService;
import com.example.victorymoments.service.UserService;
import com.example.victorymoments.config.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Posts", description = "Endpoints for managing posts")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Create a Post", description = "Create a new post with optional media files")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post created successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<PostResponse> create(
            @RequestPart("request") @Valid PostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "audios", required = false) List<MultipartFile> audios,
            HttpServletRequest requestHeader) {
        String token = requestHeader.getHeader("Authorization");
        String userId = null;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // Loại bỏ "Bearer "
            userId = jwtTokenProvider.getUserIdFromToken(token);
        }
        if (userId == null) {
            throw new SecurityException("Unauthorized: No valid token provided");
        }
        request.setUserId(userId);
        request.setImages(images);
        request.setVideos(videos);
        request.setAudios(audios);
        return ResponseEntity.ok(postService.createPost(request));
    }

    @Operation(summary = "Get All Posts", description = "Retrieve all posts with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<PostResponse>> list(Pageable pageable, HttpServletRequest requestHeader) {
        String token = requestHeader.getHeader("Authorization");
        String userId = null;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            userId = jwtTokenProvider.getUserIdFromToken(token);
        }
        return ResponseEntity.ok(postService.getAllPosts(pageable, userId));
    }

    @Operation(summary = "Get Post by ID", description = "Retrieve a specific post by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post retrieved successfully")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable String id, HttpServletRequest requestHeader) {
        String token = requestHeader.getHeader("Authorization");
        String userId = null;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            userId = jwtTokenProvider.getUserIdFromToken(token);
        }
        return ResponseEntity.ok(postService.getPostById(id, userId));
    }

    @Operation(summary = "Get Current User's Posts", description = "Retrieve posts for the authenticated user with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-posts")
    public ResponseEntity<Page<PostResponse>> getMyPosts(Pageable pageable, HttpServletRequest requestHeader) {
        String token = requestHeader.getHeader("Authorization");
        String userId = null;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            userId = jwtTokenProvider.getUserIdFromToken(token);
        }
        if (userId == null) {
            throw new SecurityException("Unauthorized: No valid token provided");
        }
        return ResponseEntity.ok(postService.getPostsForCurrentUser(pageable, userId));
    }

    @Operation(summary = "Update a Post", description = "Update an existing post with optional media files")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String id,
            @RequestPart("request") @Valid PostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "videos", required = false) List<MultipartFile> videos,
            @RequestPart(value = "audios", required = false) List<MultipartFile> audios,
            @RequestPart(value = "deletedMediaUrls", required = false) String deletedMediaUrls,
            HttpServletRequest requestHeader) {
        String token = requestHeader.getHeader("Authorization");
        String userId = null;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            userId = jwtTokenProvider.getUserIdFromToken(token);
        }
        if (userId == null) {
            throw new SecurityException("Unauthorized: No valid token provided");
        }
        request.setUserId(userId);
        request.setImages(images);
        request.setVideos(videos);
        request.setAudios(audios);
        return ResponseEntity.ok(postService.updatePost(id, request, deletedMediaUrls, userId));
    }

    @Operation(summary = "Delete a Post", description = "Delete a specific post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String id, HttpServletRequest requestHeader) {
        String token = requestHeader.getHeader("Authorization");
        String userId = null;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            userId = jwtTokenProvider.getUserIdFromToken(token);
        }
        if (userId == null) {
            throw new SecurityException("Unauthorized: No valid token provided");
        }
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle Like on a Post", description = "Like or unlike a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like status toggled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{id}/like")
    public ResponseEntity<PostResponse> toggleLike(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest requestHeader) {
        String token = requestHeader.getHeader("Authorization");
        String userId = null;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            userId = jwtTokenProvider.getUserIdFromToken(token);
        }
        if (userId == null) {
            throw new SecurityException("Unauthorized: No valid token provided");
        }
        PostResponse updatedPost = postService.toggleLike(id, userId);
        return ResponseEntity.ok(updatedPost);
    }

    @Operation(summary = "Search Users", description = "Search users by email or phone number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    })
    @GetMapping("/users/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        List<User> users = userService.searchUsersByEmailOrPhoneNumber(query);
        List<UserResponse> userResponses = users.stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }
}