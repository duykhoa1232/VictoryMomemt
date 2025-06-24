package com.example.victorymoments.service.impl;

import com.example.victorymoments.entity.Post;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.exception.BusinessException;
import com.example.victorymoments.exception.ErrorCode;
import com.example.victorymoments.exception.S3UploadException;
import com.example.victorymoments.repository.PostRepository;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.request.ProfileRequest;
import com.example.victorymoments.response.PostResponse;
import com.example.victorymoments.response.ProfileResponse;
import com.example.victorymoments.response.UserResponse;
import com.example.victorymoments.service.S3Service;
import com.example.victorymoments.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final S3Service s3Service;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .map(User::getId)
                    .orElse(null);
        }
        return null;
    }

    @Override
    @Transactional
    public User registerUser(String name, String email, String phoneNumber, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BusinessException(ErrorCode.USER_PHONE_EXISTS);
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPassword(passwordEncoder.encode(password));
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRoles(List.of("USER"));

        userRepository.save(user);
        sendWelcomeEmail(user);
        return user;
    }

    private void sendWelcomeEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Welcome to VictoryMoments!");
        message.setText(
                "Dear " + user.getName() + ",\n\n" +
                        "Welcome to VictoryMoments! Please verify your email:\n" +
                        "http://localhost:8080/api/auth/verify?token=" + user.getVerificationToken() + "\n\n" +
                        "Thank you!"
        );

        mailSender.send(message);
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFY_TOKEN_INVALID));
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    @Override
    public ProfileResponse getCurrentUserProfile(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Page<Post> userPosts = postRepository.findByUserIdAndIsActiveTrue(user.getId(), pageable);
        return mapToProfileResponse(user, userPosts.map(this::mapToPostResponse));
    }

    @Override
    public ProfileResponse getUserProfileByEmail(String emailToFetch, Pageable pageable) {
        User user = userRepository.findByEmail(emailToFetch)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Page<Post> userPosts = postRepository.findByUserIdAndIsActiveTrue(user.getId(), pageable);
        return mapToProfileResponse(user, userPosts.map(this::mapToPostResponse));
    }

    @Override
    @Transactional
    public ProfileResponse updateUserProfile(String userEmail, ProfileRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber()) &&
                    !user.getPhoneNumber().equals(request.getPhoneNumber())) {
                throw new BusinessException(ErrorCode.USER_PHONE_EXISTS);
            }
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        user.setUpdatedAt(LocalDateTime.now());
        return mapToProfileResponse(userRepository.save(user), Page.empty());
    }

    @Override
    @Transactional
    public ProfileResponse updateUserAvatar(String userEmail, MultipartFile avatarFile) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED); // Hoặc bạn thêm ErrorCode riêng
        }
        try {
            // Delete old avatar if exists
            if (user.getAvatarUrl() != null) {
                s3Service.deleteFile(extractS3KeyFromUrl(user.getAvatarUrl()));
            }
            String newAvatarUrl = s3Service.uploadFile(avatarFile, "avatars");
            user.setAvatarUrl(newAvatarUrl);
            user.setUpdatedAt(LocalDateTime.now());
            return mapToProfileResponse(userRepository.save(user), Page.empty());
        } catch (S3UploadException e) {
            throw new BusinessException(ErrorCode.POST_DELETE_S3_FAILED);
        }
    }

    @Override
    public List<User> searchUsersByEmailOrPhoneNumber(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return userRepository.findByEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(query, query);
    }

    private ProfileResponse mapToProfileResponse(User user, Page<PostResponse> userPosts) {
        return ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .enabled(user.isEnabled())
                .userPosts(userPosts)
                .build();
    }

    private PostResponse mapToPostResponse(Post post) {
        User author = userRepository.findById(post.getUserId()).orElse(null);
        UserResponse authorResponse = null;
        if (author != null) {
            authorResponse = UserResponse.builder()
                    .id(author.getId())
                    .name(author.getName())
                    .email(author.getEmail())
                    .avatarUrl(author.getAvatarUrl())
                    .build();
        }
        boolean isLikedByCurrentUser = false;
        String currentUserId = getCurrentUserId();
        if (currentUserId != null && post.getLikedByUsers() != null) {
            isLikedByCurrentUser = post.getLikedByUsers().contains(currentUserId);
        }

        return PostResponse.builder()
                .id(post.getId())
                .author(authorResponse)
                .content(post.getContent())
                .audioUrls(post.getMediaIds())
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
        try {
            URI uri = new URI(s3Url);
            String path = uri.getPath();
            return (path != null && path.startsWith("/")) ? path.substring(1) : path;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.POST_DELETE_S3_FAILED);
        }
    }
}
