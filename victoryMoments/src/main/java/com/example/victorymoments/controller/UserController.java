package com.example.victorymoments.controller;

import com.example.victorymoments.dto.ProfileRequest;
import com.example.victorymoments.dto.ProfileResponse;
import com.example.victorymoments.dto.UserResponse;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.service.UserService;
import com.example.victorymoments.util.SecurityUtil;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Users", description = "Endpoints for managing user profiles and searching users")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Get Current User Profile with Posts",
            description = "Retrieve the profile (name, bio, avatar) and paginated posts of the currently authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User profile not found")
    })
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/users/profile")
    public ResponseEntity<ProfileResponse> getCurrentUserProfile(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String currentUsername = SecurityUtil.getCurrentUsername();
        return ResponseEntity.ok(userService.getCurrentUserProfile(currentUsername, pageable));
    }

    @Operation(summary = "Get User Profile by Email with Posts", description = "Retrieve the profile (name, bio, avatar) and paginated posts of any user by their email. Publicly accessible.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userEmail}/profile")
    public ResponseEntity<ProfileResponse> getUserProfileByEmail(
            @PathVariable String userEmail,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.getUserProfileByEmail(userEmail, pageable));
    }

    @Operation(summary = "Update User Profile (Text Fields)", description = "Update the currently authenticated user's profile information (name, phone, bio).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input or validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User attempting to update another user's profile"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/users/{userEmail}/profile")
    public ResponseEntity<ProfileResponse> updateUserProfile(
            @PathVariable String userEmail,
            @Valid @RequestBody ProfileRequest request) {
        String currentUsername = SecurityUtil.getCurrentUsername();
        if (!currentUsername.equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.updateUserProfile(userEmail, request));
    }

    @Operation(summary = "Update User Avatar", description = "Update the currently authenticated user's avatar image.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid file"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User attempting to update another user's avatar"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/users/{userEmail}/avatar")
    public ResponseEntity<ProfileResponse> updateUserAvatar(
            @PathVariable String userEmail,
            @RequestParam("file") MultipartFile avatarFile) {
        String currentUsername = SecurityUtil.getCurrentUsername();
        if (!currentUsername.equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.updateUserAvatar(userEmail, avatarFile));
    }

    @Operation(summary = "Search Users", description = "Search users by email or phone number (partial match, case-insensitive). Publicly accessible.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class)))
    })
    @GetMapping("/users/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        List<User> users = userService.searchUsers(query);
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


    @Operation(summary = "Find Users to Befriend", description = "Search for users by name, email, or phone number to send friend requests. Requires authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/users/find-friends")
    public ResponseEntity<List<UserResponse>> findUsersToBefriend(@RequestParam String query) {
        List<User> users = userService.searchUsers(query);
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