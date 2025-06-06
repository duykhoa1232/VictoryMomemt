package com.example.victorymoments.service;

import com.example.victorymoments.dto.ProfileRequest;
import com.example.victorymoments.dto.ProfileResponse;
import com.example.victorymoments.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    ProfileResponse getUserProfile(String userEmail);
    ProfileResponse updateUserProfile(String userEmail, ProfileRequest request);
    ProfileResponse updateUserAvatar(String userEmail, MultipartFile avatarFile);
    List<User> searchUsersByEmailOrPhoneNumber(String query);
}
