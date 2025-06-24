package com.example.victorymoments.controller;

import com.example.victorymoments.request.ShareRequest;
import com.example.victorymoments.response.ApiErrorResponse;
import com.example.victorymoments.response.ShareResponse;
import com.example.victorymoments.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Shares", description = "Endpoints for sharing and managing shared posts")
@RestController
@RequestMapping("/api/shares")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @Operation(
            summary = "Share a Post",
            description = "Create a new share (repost) for an existing original post."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Post shared successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShareResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid input or post already shared",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - User not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Original post not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping
    public ResponseEntity<ShareResponse> sharePost(
            @Valid @RequestBody ShareRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        ShareResponse response = shareService.sharePost(request, currentUser.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Unshare a Post",
            description = "Deactivates a previously shared post. Only the sharer or an admin can perform this."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post unshared successfully"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - User not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User not authorized to unshare",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Shared post not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{shareId}")
    public ResponseEntity<Void> unsharePost(
            @Parameter(description = "ID of the shared post to unshare") @PathVariable String shareId,
            @AuthenticationPrincipal UserDetails currentUser) {
        shareService.unsharePost(shareId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get Shared Post by ID",
            description = "Retrieve details of a specific shared post by its ID. Publicly accessible."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Shared post retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShareResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Shared post not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{shareId}")
    public ResponseEntity<ShareResponse> getShareById(
            @Parameter(description = "ID of the shared post") @PathVariable String shareId) {
        return ResponseEntity.ok(shareService.getShareById(shareId));
    }

    @Operation(
            summary = "Get Shares by User",
            description = "Retrieve a page of shared posts created by a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Shares retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShareResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid page or size parameters",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ShareResponse>> getSharesByUser(
            @Parameter(description = "User ID to list shares for") @PathVariable String userId,
            @Parameter(description = "Pagination information") Pageable pageable) {
        return ResponseEntity.ok(shareService.getSharesByUserId(userId, pageable));
    }
}
