package com.example.victorymoments.controller;

import com.example.victorymoments.dto.ProfileRequest;
import com.example.victorymoments.dto.ProfileResponse;
import com.example.victorymoments.dto.UserResponse;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Users", description = "Endpoints for managing user profiles")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get User Profile", description = "Retrieve a user's profile with their posts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProfileResponse.class)))
    })
    @GetMapping("/users/{userEmail}")
    public ResponseEntity<ProfileResponse> getUserProfile(@PathVariable String userEmail, Pageable pageable) {
        return ResponseEntity.ok(userService.getUserProfile(userEmail, pageable));
    }

    @Operation(summary = "Update User Profile", description = "Update a user's profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/users/{userEmail}")
    public ResponseEntity<ProfileResponse> updateUserProfile(
            @PathVariable String userEmail,
            @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(userEmail, request));
    }

    @Operation(summary = "Update User Avatar", description = "Update a user's avatar image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar updated successfully", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/users/{userEmail}/avatar")
    public ResponseEntity<ProfileResponse> updateUserAvatar(
            @PathVariable String userEmail,
            @RequestParam("file") MultipartFile avatarFile) {
        return ResponseEntity.ok(userService.updateUserAvatar(userEmail, avatarFile));
    }

    @Operation(summary = "Search Users", description = "Search users by email or phone number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UserResponse.class)))
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