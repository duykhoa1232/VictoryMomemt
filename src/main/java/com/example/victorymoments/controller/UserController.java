//package com.example.victorymoments.controller;
//
//import com.example.victorymoments.dto.ProfileRequest;
//import com.example.victorymoments.dto.ProfileResponse;
//import com.example.victorymoments.dto.UserResponse;
//import com.example.victorymoments.entity.User;
//import com.example.victorymoments.service.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//import java.util.stream.Collectors;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//
//@Tag(name = "Users", description = "Endpoints for managing user profiles")
//@RestController
//@RequestMapping("/api")
//@RequiredArgsConstructor
//public class UserController {
//
//    private final UserService userService;
//
//    @Operation(summary = "Get User Profile", description = "Retrieve a user's profile with their posts")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProfileResponse.class)))
//    })
//    @GetMapping("/users/{userEmail}")
//    public ResponseEntity<ProfileResponse> getUserProfile(@PathVariable String userEmail, Pageable pageable) {
//        return ResponseEntity.ok(userService.getUserProfile(userEmail, pageable));
//    }
//
//    @Operation(summary = "Update User Profile", description = "Update a user's profile information")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Profile updated successfully", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProfileResponse.class))),
//            @ApiResponse(responseCode = "401", description = "Unauthorized")
//    })
//    @PutMapping("/users/{userEmail}")
//    public ResponseEntity<ProfileResponse> updateUserProfile(
//            @PathVariable String userEmail,
//            @RequestBody ProfileRequest request) {
//        return ResponseEntity.ok(userService.updateUserProfile(userEmail, request));
//    }
//
//    @Operation(summary = "Update User Avatar", description = "Update a user's avatar image")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Avatar updated successfully", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProfileResponse.class))),
//            @ApiResponse(responseCode = "401", description = "Unauthorized")
//    })
//    @PostMapping("/users/{userEmail}/avatar")
//    public ResponseEntity<ProfileResponse> updateUserAvatar(
//            @PathVariable String userEmail,
//            @RequestParam("file") MultipartFile avatarFile) {
//        return ResponseEntity.ok(userService.updateUserAvatar(userEmail, avatarFile));
//    }
//
//    @Operation(summary = "Search Users", description = "Search users by email or phone number")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UserResponse.class)))
//    })
//    @GetMapping("/users/search")
//    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
//        List<User> users = userService.searchUsersByEmailOrPhoneNumber(query);
//        List<UserResponse> userResponses = users.stream()
//                .map(user -> UserResponse.builder()
//                        .id(user.getId())
//                        .name(user.getName())
//                        .email(user.getEmail())
//                        .avatarUrl(user.getAvatarUrl())
//                        .build())
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(userResponses);
//    }
//}


package com.example.victorymoments.controller;

import com.example.victorymoments.dto.ProfileRequest;
import com.example.victorymoments.dto.ProfileResponse;
import com.example.victorymoments.dto.UserResponse;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get Current User Profile with Posts", description = "Retrieve the profile (name, bio, avatar) and paginated posts of the currently authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
        @ApiResponse(responseCode = "404", description = "User profile not found")
    })
    @GetMapping("/users/profile")
    public ResponseEntity<ProfileResponse> getCurrentUserProfile(
        @AuthenticationPrincipal UserDetails currentUser,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.getCurrentUserProfile(currentUser.getUsername(), pageable));
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

    @Operation(summary = "Update User Profile (Text Fields)", description = "Update the currently authenticated user's profile information (name, phone, bio). The userEmail in the path must match the authenticated user's email for authorization.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input or validation failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated or not authorized to update this profile"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User attempting to update another user's profile"), // Thêm code này
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/users/{userEmail}/profile")
    public ResponseEntity<ProfileResponse> updateUserProfile(
        @PathVariable String userEmail,
        @Valid @RequestBody ProfileRequest request,
        @AuthenticationPrincipal UserDetails currentUser) {
        if (!currentUser.getUsername().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.updateUserProfile(userEmail, request));
    }


    @Operation(summary = "Update User Avatar", description = "Update the currently authenticated user's avatar image. The userEmail in the path must match the authenticated user's email for authorization.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Avatar updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid file"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated or not authorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User attempting to update another user's avatar"), // Thêm code này
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/users/{userEmail}/avatar")
    public ResponseEntity<ProfileResponse> updateUserAvatar(
        @PathVariable String userEmail,
        @RequestParam("file") MultipartFile avatarFile,
        @AuthenticationPrincipal UserDetails currentUser) {
        if (!currentUser.getUsername().equals(userEmail)) {
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
