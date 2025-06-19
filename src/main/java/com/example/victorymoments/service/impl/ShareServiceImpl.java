package com.example.victorymoments.service.impl;

import com.example.victorymoments.dto.PostResponse;
import com.example.victorymoments.dto.ShareRequest;
import com.example.victorymoments.dto.ShareResponse;
import com.example.victorymoments.dto.UserResponse;
import com.example.victorymoments.entity.Post;
import com.example.victorymoments.entity.Share;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.repository.PostRepository;
import com.example.victorymoments.repository.ShareRepository;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private static final Logger logger = LoggerFactory.getLogger(ShareServiceImpl.class);

    private final ShareRepository shareRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String userEmail = ((UserDetails) principal).getUsername();
            Optional<User> userOptional = userRepository.findByEmail(userEmail);
            return userOptional.map(User::getId).orElse(null);
        }
        return null;
    }

    @Override
    @Transactional
    public ShareResponse sharePost(ShareRequest request, String userEmail) {
        Post originalPost = postRepository.findByIdAndIsActiveTrue(request.getOriginalPostId())
                .orElseThrow(() -> new RuntimeException("Original post not found or is inactive with ID: " + request.getOriginalPostId()));

        User sharingUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Sharing user not found: " + userEmail));

        Share share = Share.builder()
                .originalPostId(originalPost.getId())
                .sharedByUserId(sharingUser.getId())
                .sharedByUserEmail(sharingUser.getEmail())
                .sharedByUserName(sharingUser.getName())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        share = shareRepository.save(share);

        originalPost.setShareCount(originalPost.getShareCount() + 1);
        postRepository.save(originalPost);

        return mapToShareResponse(share, originalPost, sharingUser);
    }

    @Override
    @Transactional
    public void unsharePost(String shareId, String userEmail) {
        Share share = shareRepository.findByIdAndIsActiveTrue(shareId)
                .orElseThrow(() -> new RuntimeException("Shared post not found with ID: " + shareId));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        if (!share.getSharedByUserEmail().equals(userEmail) && !currentUser.getRoles().contains("ADMIN")) {
            throw new RuntimeException("You are not authorized to unshare this post.");
        }

        share.setActive(false);
        share.setUpdatedAt(LocalDateTime.now());
        shareRepository.save(share);

        logger.info("Shared post {} unshared by user {}. Original post share count remains unchanged.", shareId, userEmail);
    }

    @Override
    public Page<ShareResponse> getSharesByUserId(String userId, Pageable pageable) {
        Page<Share> sharesPage = shareRepository.findBySharedByUserIdAndIsActiveTrue(userId, pageable);

        List<ShareResponse> shareResponses = sharesPage.getContent().stream()
                .map(share -> {
                    Post originalPost = postRepository.findByIdAndIsActiveTrue(share.getOriginalPostId())
                            .orElse(null);
                    User sharingUser = userRepository.findById(share.getSharedByUserId())
                            .orElse(null);

                    return mapToShareResponse(share, originalPost, sharingUser);
                })
                .filter(sr -> sr.getOriginalPost() != null)
                .collect(Collectors.toList());

        return new PageImpl<>(shareResponses, pageable, sharesPage.getTotalElements());
    }


    @Override
    public ShareResponse getShareById(String shareId) {
        Share share = shareRepository.findByIdAndIsActiveTrue(shareId)
                .orElseThrow(() -> new RuntimeException("Shared post not found with ID: " + shareId));

        Post originalPost = postRepository.findByIdAndIsActiveTrue(share.getOriginalPostId())
                .orElseThrow(() -> new RuntimeException("Original post not found for share: " + share.getOriginalPostId()));

        User sharingUser = userRepository.findById(share.getSharedByUserId())
                .orElseThrow(() -> new RuntimeException("Sharing user not found for share: " + share.getSharedByUserId()));

        return mapToShareResponse(share, originalPost, sharingUser);
    }

    private ShareResponse mapToShareResponse(Share share, Post originalPost, User sharingUser) {
        UserResponse sharedByUserResponse = null;
        if (sharingUser != null) {
            sharedByUserResponse = UserResponse.builder()
                    .id(sharingUser.getId())
                    .name(sharingUser.getName())
                    .email(sharingUser.getEmail())
                    .avatarUrl(sharingUser.getAvatarUrl())
                    .build();
        }

        PostResponse originalPostResponse = null;
        if (originalPost != null) {
            originalPostResponse = mapToPostResponse(originalPost);
        }

        return ShareResponse.builder()
                .id(share.getId())
                .originalPostId(share.getOriginalPostId())
                .sharedBy(sharedByUserResponse)
                .content(share.getContent())
                .originalPost(originalPostResponse)
                .createdAt(share.getCreatedAt())
                .updatedAt(share.getUpdatedAt())
                .isActive(share.isActive())
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
        boolean isLikedByCurrentUser = currentUserId != null && post.getLikedByUsers() != null && post.getLikedByUsers().contains(currentUserId);

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
}
