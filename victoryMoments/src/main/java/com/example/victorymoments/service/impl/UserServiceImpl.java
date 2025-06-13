package com.example.victorymoments.service.impl;

import com.example.victorymoments.dto.*;
import com.example.victorymoments.entity.Post;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.exception.S3UploadException;
import com.example.victorymoments.repository.PostRepository;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.service.S3Service;
import com.example.victorymoments.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final S3Service s3Service;
    private final JavaMailSender mailSender; // Thêm dependency để gửi email
    private final PasswordEncoder passwordEncoder; // Thêm để mã hóa mật khẩu

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
    public User registerUser(String name, String email, String phoneNumber, String password) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        // Tạo user mới
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPassword(passwordEncoder.encode(password)); // Mã hóa mật khẩu
        user.setVerificationToken(UUID.randomUUID().toString()); // Tạo token ngẫu nhiên
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Lưu user vào database
        userRepository.save(user);

        // Gửi email chào mừng
        sendWelcomeEmail(user);

        return user;
    }

    private void sendWelcomeEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Welcome to VictoryMoments!");
        message.setText("Dear " + user.getName() + ",\n\n" +
                "Welcome to VictoryMoments! We are excited to have you on board.\n\n" +
                "Please click the link below to verify your email:\n" +
                "http://localhost:8080/api/auth/verify?token=" + user.getVerificationToken() + "\n\n" +
                "Thank you for joining us!\n" +
                "VictoryMoments Team");

        mailSender.send(message);
    }

    @Override
    public boolean verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    @Override
    public ProfileResponse getUserProfile(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        Page<Post> userPostsPage = postRepository.findByUserIdAndIsActiveTrue(user.getId(), pageable);

        Page<PostResponse> userPostResponsesPage = userPostsPage.map(this::mapToPostResponse);

        return mapToProfileResponse(user, userPostResponsesPage);
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

        Page<Post> userPostsPage = Page.empty(); // Không tải lại posts để tối ưu
        Page<PostResponse> userPostResponsesPage = new PageImpl<>(List.of(), Pageable.unpaged(), 0);

        return mapToProfileResponse(updatedUser, userPostResponsesPage);
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

            Page<Post> userPostsPage = Page.empty(); // Không tải lại posts để tối ưu
            Page<PostResponse> userPostResponsesPage = new PageImpl<>(List.of(), Pageable.unpaged(), 0);

            return mapToProfileResponse(updatedUser, userPostResponsesPage);

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

    private ProfileResponse mapToProfileResponse(User user, Page<PostResponse> userPostResponses) {
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

    private String extractS3KeyFromUrl(String s3Url) {
        int startIndex = s3Url.indexOf(".com/") + 5;
        if (startIndex > 5) {
            return s3Url.substring(startIndex);
        }
        throw new IllegalArgumentException("Invalid S3 URL format: " + s3Url);
    }
}