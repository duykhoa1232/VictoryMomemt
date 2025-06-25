package com.example.victorymoments.controller;

import com.example.victorymoments.entity.User;
import com.example.victorymoments.exception.ErrorCode;
import com.example.victorymoments.exception.UnauthorizedException;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.request.PostRequest;
import com.example.victorymoments.response.ApiErrorResponse;
import com.example.victorymoments.response.PostResponse;
import com.example.victorymoments.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = {"https://shark-calm-externally.ngrok-free.app", "http://localhost:4200"})
@Tag(name = "Posts", description = "Endpoints for managing posts")
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Get list of posts",
            description = "Retrieve a list of posts with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of posts retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = getAuthenticatedUserId(userDetails);
        Page<PostResponse> posts = postService.getAllPosts(pageable, userId);
        return ResponseEntity.ok(posts);
    }


    private String getAuthenticatedUserId(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID);
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.USER_NOT_FOUND, userDetails.getUsername()));
    }

    @Operation(
            summary = "Create a Post",
            description = "Create a new post with optional image, video, and audio files"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Post created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PostResponse> create(
            @ModelAttribute @Valid PostRequest request,
            @Parameter(description = "Images to attach to the post")
            @RequestPart(name = "images", required = false) List<MultipartFile> imageFiles,
            @Parameter(description = "Videos to attach to the post")
            @RequestPart(name = "videos", required = false) List<MultipartFile> videoFiles,
            @Parameter(description = "Audios to attach to the post")
            @RequestPart(name = "audios", required = false) List<MultipartFile> audioFiles,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getAuthenticatedUserId(userDetails);
        request.setImages(imageFiles != null ? imageFiles : List.of());
        request.setVideos(videoFiles != null ? videoFiles : List.of());
        request.setAudios(audioFiles != null ? audioFiles : List.of());

        return ResponseEntity.ok(postService.createPost(request));
    }

    @Operation(
            summary = "Update a Post",
            description = "Update an existing post's content and attached media"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Post updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Post not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "ID of the post to update") @PathVariable String id,
            @ModelAttribute @Valid PostRequest request,
            @RequestPart(name = "images", required = false) List<MultipartFile> imageFiles,
            @RequestPart(name = "videos", required = false) List<MultipartFile> videoFiles,
            @RequestPart(name = "audios", required = false) List<MultipartFile> audioFiles,
            @RequestPart(name = "deletedMediaUrls", required = false) String deletedMediaUrls,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = getAuthenticatedUserId(userDetails);
        request.setImages(imageFiles != null ? imageFiles : List.of());
        request.setVideos(videoFiles != null ? videoFiles : List.of());
        request.setAudios(audioFiles != null ? audioFiles : List.of());

        return ResponseEntity.ok(postService.updatePost(id, request, deletedMediaUrls, userId));
    }

    @Operation(
            summary = "Delete a Post",
            description = "Delete a specific post by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Post deleted successfully"),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Post not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "ID of the post to delete") @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        postService.deletePost(id, getAuthenticatedUserId(userDetails));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Toggle Like on a Post",
            description = "Like or unlike a specific post"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Toggled like successfully",
                    content = @Content(
                            schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Post not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{id}/like")
    public ResponseEntity<PostResponse> toggleLike(
            @Parameter(description = "ID of the post to like or unlike") @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        PostResponse updatedPost = postService.toggleLike(id, getAuthenticatedUserId(userDetails));
        return ResponseEntity.ok(updatedPost);
    }

    @GetMapping("/by-user-email")
    public Page<PostResponse> getPostsByUserEmail(
            @RequestParam("userEmail") String userEmail,
            Pageable pageable
    ) {
        return postService.getPostsByEmail(userEmail, pageable);
    }
}
