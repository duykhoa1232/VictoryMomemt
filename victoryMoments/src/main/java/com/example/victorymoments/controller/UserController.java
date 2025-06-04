package com.example.victorymoments.controller;

import com.example.victorymoments.dto.ProfileRequest;
import com.example.victorymoments.dto.ProfileResponse;
import com.example.victorymoments.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getUserProfile(
            @AuthenticationPrincipal UserDetails currentUser) {
        ProfileResponse profile = userService.getUserProfile(currentUser.getUsername());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateUserProfile(
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody ProfileRequest request) {
        ProfileResponse updatedProfile = userService.updateUserProfile(currentUser.getUsername(), request);
        return ResponseEntity.ok(updatedProfile);
    }
    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> updateUserAvatar(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam("avatarFile") MultipartFile avatarFile) {
        ProfileResponse updatedProfile = userService.updateUserAvatar(currentUser.getUsername(), avatarFile);
        return ResponseEntity.ok(updatedProfile);
    }

}