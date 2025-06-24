package com.example.victorymoments.controller;


import com.example.victorymoments.entity.User;
import com.example.victorymoments.request.ProfileRequest;
import com.example.victorymoments.response.ApiErrorResponse;
import com.example.victorymoments.response.ProfileResponse;
import com.example.victorymoments.response.UserResponse;
import com.example.victorymoments.service.PostService;
import com.example.victorymoments.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Users", description = "Endpoints for managing user profiles and searching users")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;

    @Operation(
            summary = "Get Current User Profile with Posts",
            description = "Retrieve the profile (name, bio, avatar) and paginated posts of the currently authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - User not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getCurrentUserProfile(
            @AuthenticationPrincipal UserDetails currentUser,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(
                userService.getCurrentUserProfile(currentUser.getUsername(), pageable));
    }

    @Operation(
            summary = "Get User Profile by Email with Posts",
            description = "Retrieve profile and paginated posts of any user by their email. Publicly accessible."
    )
    @GetMapping("/{userEmail}/profile")
    public ResponseEntity<ProfileResponse> getUserProfileByEmail(
            @PathVariable String userEmail,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(
                userService.getUserProfileByEmail(userEmail, pageable));
    }

    @Operation(
            summary = "Update User Profile (Text Fields)",
            description = "Update the currently authenticated user's profile information."
    )
    @PutMapping("/{userEmail}/profile")
    public ResponseEntity<ProfileResponse> updateUserProfile(
            @PathVariable String userEmail,
            @Valid @RequestBody ProfileRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        if (!currentUser.getUsername().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                userService.updateUserProfile(userEmail, request));
    }

    @Operation(
            summary = "Update User Avatar",
            description = "Update the currently authenticated user's avatar image."
    )
    @PostMapping("/{userEmail}/avatar")
    public ResponseEntity<ProfileResponse> updateUserAvatar(
            @PathVariable String userEmail,
            @RequestParam("file") MultipartFile avatarFile,
            @AuthenticationPrincipal UserDetails currentUser) {

        if (!currentUser.getUsername().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                userService.updateUserAvatar(userEmail, avatarFile));
    }

    @Operation(
            summary = "Search Users",
            description = "Search users by email or phone number."
    )
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @RequestParam String query) {

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

//    @Operation(
//            summary = "Get Posts by User Email",
//            description = "Retrieve paginated posts created by the specified user email."
//    )
//    @GetMapping("/by-user-email")
//    public ResponseEntity<?> getPostsByUserEmail(
//            @RequestParam String userEmail,
//            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
//            @AuthenticationPrincipal UserDetails currentUser) {
//
//        return ResponseEntity.ok(
//                postService.getPostsByEmail(
//                        userEmail,
//                        pageable,
//                        currentUser != null ? currentUser.getUsername() : null
//                )
//        );
//    }
}
