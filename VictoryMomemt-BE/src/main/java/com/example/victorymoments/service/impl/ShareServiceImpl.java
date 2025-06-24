package com.example.victorymoments.service.impl;

import com.example.victorymoments.entity.Post;
import com.example.victorymoments.entity.Share;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.entity.VisibilityStatus;
import com.example.victorymoments.exception.*;
import com.example.victorymoments.repository.PostRepository;
import com.example.victorymoments.repository.ShareRepository;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.request.ShareRequest;
import com.example.victorymoments.response.PostResponse;
import com.example.victorymoments.response.ShareResponse;
import com.example.victorymoments.response.UserResponse;
import com.example.victorymoments.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private static final Logger logger = LoggerFactory.getLogger(ShareServiceImpl.class);

    private final ShareRepository shareRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    @Override
    @Transactional
    public ShareResponse sharePost(ShareRequest request, String userEmail) {
        Post originalPost = postRepository.findByIdAndIsActiveTrue(request.getOriginalPostId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND, request.getOriginalPostId()));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userEmail));

        if (originalPost.getVisibilityStatus() == VisibilityStatus.PRIVATE &&
                !originalPost.getAuthorizedViewerIds().contains(user.getId())) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN_ACTION);
        }

        Share share = Share.builder()
                .originalPostId(originalPost.getId())
                .sharedByUserId(user.getId())
                .sharedByUserEmail(user.getEmail())
                .sharedByUserName(user.getName())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        shareRepository.save(share);

        originalPost.setShareCount(originalPost.getShareCount() + 1);
        postRepository.save(originalPost);

        return mapToShareResponse(share, originalPost, user);
    }

    @Override
    @Transactional
    public void unsharePost(String shareId, String userEmail) {
        Share share = shareRepository.findByIdAndIsActiveTrue(shareId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SHARE_NOT_FOUND, shareId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userEmail));

        boolean isOwner = share.getSharedByUserEmail().equals(userEmail);
        boolean isAdmin = user.getRoles().contains("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN_ACTION);
        }

        share.setActive(false);
        share.setUpdatedAt(LocalDateTime.now());
        shareRepository.save(share);

        logger.info("Share {} đã bị unshare bởi {}", shareId, userEmail);
    }

    @Override
    public ShareResponse getShareById(String shareId) {
        Share share = shareRepository.findByIdAndIsActiveTrue(shareId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SHARE_NOT_FOUND, shareId));

        Post originalPost = postRepository.findByIdAndIsActiveTrue(share.getOriginalPostId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND, share.getOriginalPostId()));

        User sharingUser = userRepository.findById(share.getSharedByUserId()).orElse(null);

        return mapToShareResponse(share, originalPost, sharingUser);
    }

    @Override
    public Page<ShareResponse> getSharesByUserId(String userId, Pageable pageable) {
        Page<Share> sharePage = shareRepository.findBySharedByUserIdAndIsActiveTrue(userId, pageable);

        List<ShareResponse> shareResponses = sharePage.getContent().stream()
                .map(share -> {
                    Post post = postRepository.findByIdAndIsActiveTrue(share.getOriginalPostId()).orElse(null);
                    User user = userRepository.findById(share.getSharedByUserId()).orElse(null);
                    return mapToShareResponse(share, post, user);
                })
                .filter(sr -> sr.getOriginalPost() != null)
                .collect(Collectors.toList());

        return new PageImpl<>(shareResponses, pageable, sharePage.getTotalElements());
    }

    private ShareResponse mapToShareResponse(Share share, Post originalPost, User user) {
        UserResponse sharedBy = user != null ? UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build() : null;

        PostResponse postResponse = originalPost != null ? mapToPostResponse(originalPost) : null;

        return ShareResponse.builder()
                .id(share.getId())
                .originalPostId(share.getOriginalPostId())
                .sharedBy(sharedBy)
                .content(share.getContent())
                .originalPost(postResponse)
                .createdAt(share.getCreatedAt())
                .updatedAt(share.getUpdatedAt())
                .isActive(share.isActive())
                .build();
    }

    private PostResponse mapToPostResponse(Post post) {
        UserResponse author = userRepository.findById(post.getUserId())
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .orElse(null);

        return PostResponse.builder()
                .id(post.getId())
                .author(author)
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
                .build();
    }
}
