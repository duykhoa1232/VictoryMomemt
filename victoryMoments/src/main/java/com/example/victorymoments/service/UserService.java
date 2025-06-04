package com.example.victorymoments.service;

import com.example.victorymoments.dto.ProfileRequest;
import com.example.victorymoments.dto.ProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    ProfileResponse getUserProfile(String userEmail);
    ProfileResponse updateUserProfile(String userEmail, ProfileRequest request);
    ProfileResponse updateUserAvatar(String userEmail, MultipartFile avatarFile);
}
