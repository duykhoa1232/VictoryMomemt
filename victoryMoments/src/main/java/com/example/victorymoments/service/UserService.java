package com.example.victorymoments.service;

import com.example.victorymoments.dto.ProfileRequest;
import com.example.victorymoments.dto.ProfileResponse;
import com.example.victorymoments.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    User registerUser(String name, String email, String phoneNumber, String password);

    boolean verifyEmail(String token);

    ProfileResponse getCurrentUserProfile(String userEmail, Pageable pageable);

    ProfileResponse getUserProfileByEmail(String emailToFetch, Pageable pageable);

    ProfileResponse updateUserProfile(String userEmail, ProfileRequest request);

    ProfileResponse updateUserAvatar(String userEmail, MultipartFile avatarFile);

    List<User> searchUsers(String query);
}