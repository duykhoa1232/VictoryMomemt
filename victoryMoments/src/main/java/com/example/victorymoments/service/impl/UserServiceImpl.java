package com.example.victorymoments.service.impl;

import com.example.victorymoments.dto.PostResponse;
import com.example.victorymoments.dto.ProfileRequest;
import com.example.victorymoments.dto.ProfileResponse;
import com.example.victorymoments.dto.UserResponse;
import com.example.victorymoments.entity.Post;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.exception.S3UploadException;
import com.example.victorymoments.repository.PostRepository;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.service.S3Service;
import com.example.victorymoments.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final S3Service s3Service;

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String userEmail = ((UserDetails) principal).getUsername();
            Optional<User> userOptional = userRepository.findByEmail(userEmail);
            if (userOptional.isPresent()) {
                return userOptional.get().getId();
            }
        }
        return null;
    }

    @Override
    public ProfileResponse getUserProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        List<Post> userPosts = postRepository.findByUserIdAndIsActiveTrue(user.getId());

        List<PostResponse> userPostResponses = userPosts.stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());

        return mapToProfileResponse(user, userPostResponses);
    }

    @Override
    public ProfileResponse updateUserProfile(String userEmail, ProfileRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber()) &&
                    !user.getPhoneNumber().equals(request.getPhoneNumber())) {
                throw new RuntimeException("Phone number is already in use by another user.");
            }
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        List<Post> userPosts = postRepository.findByUserIdAndIsActiveTrue(updatedUser.getId());
        List<PostResponse> userPostResponses = userPosts.stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());

        return mapToProfileResponse(updatedUser, userPostResponses);
    }

    @Override
    public ProfileResponse updateUserAvatar(String userEmail, MultipartFile avatarFile) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new IllegalArgumentException("Avatar file cannot be empty.");
        }

        try {
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                String oldAvatarKey = extractS3KeyFromUrl(user.getAvatarUrl());
                s3Service.deleteFile(oldAvatarKey);
            }

            String newAvatarUrl = s3Service.uploadFile(avatarFile, "avatars");

            user.setAvatarUrl(newAvatarUrl);
            user.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(user);

            List<Post> userPosts = postRepository.findByUserIdAndIsActiveTrue(updatedUser.getId());
            List<PostResponse> userPostResponses = userPosts.stream()
                    .map(this::mapToPostResponse)
                    .collect(Collectors.toList());

            return mapToProfileResponse(updatedUser, userPostResponses);

        } catch (S3UploadException e) {
            throw new RuntimeException("Failed to upload/delete avatar file on S3: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> searchUsersByEmailOrPhoneNumber(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return userRepository.findByEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(query, query);
    }



    private PostResponse mapToPostResponse(Post post) {
        Optional<User> authorOptional = userRepository.findById(post.getUserId());
        UserResponse authorResponse = null;
        if (authorOptional.isPresent()) {
            User authorEntity = authorOptional.get();
            authorResponse = UserResponse.builder()
                    .id(authorEntity.getId())
                    .name(authorEntity.getName())
                    .email(authorEntity.getEmail())
                    .avatarUrl(authorEntity.getAvatarUrl())
                    .build();
        }

        String currentUserId = getCurrentUserId();
        boolean isLikedByCurrentUser = currentUserId != null && post.getLikedByUsers().contains(currentUserId);


        return PostResponse.builder()
                .id(post.getId())
                .author(authorResponse)
                .content(post.getContent())
                .imageUrls(post.getImageUrls())
                .videoUrls(post.getVideoUrls())
                .audioUrls(post.getAudioUrls())
                .location(post.getLocation())
                .visibilityStatus(post.getVisibilityStatus())
                .authorizedViewerIds(post.getAuthorizedViewerIds())
                .tags(post.getTags())
                .likeCount(post.getLikeCount())
                .likedByUsers(post.getLikedByUsers())
                .commentCount(post.getCommentCount())
                .shareCount(post.getShareCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isActive(post.isActive())
                .isLikedByCurrentUser(isLikedByCurrentUser)
                .build();
    }

    private ProfileResponse mapToProfileResponse(User user, List<PostResponse> userPostResponses) {
        return ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .enabled(user.isEnabled())
                .userPosts(userPostResponses)
                .build();
    }

    private String extractS3KeyFromUrl(String s3Url) {
        int startIndex = s3Url.indexOf(".com/") + 5;
        if (startIndex > 5) {
            return s3Url.substring(startIndex);
        }
        throw new IllegalArgumentException("Invalid S3 URL format: " + s3Url);
    }
}