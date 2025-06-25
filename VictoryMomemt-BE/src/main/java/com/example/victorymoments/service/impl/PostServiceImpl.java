package com.example.victorymoments.service.impl;

import com.example.victorymoments.entity.*;
import com.example.victorymoments.exception.*;
import com.example.victorymoments.repository.PostRepository;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.request.PostRequest;
import com.example.victorymoments.response.CommentResponse;
import com.example.victorymoments.response.PostResponse;
import com.example.victorymoments.response.UserResponse;
import com.example.victorymoments.service.CommentService;
import com.example.victorymoments.service.MediaService;
import com.example.victorymoments.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MediaService mediaService;
    private final CommentService commentService;
    private final ObjectMapper objectMapper;

    public PostServiceImpl(
            PostRepository postRepository,
            UserRepository userRepository,
            CommentService commentService,
            MediaService mediaService,
            ObjectMapper objectMapper
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentService = commentService;
        this.mediaService = mediaService;
        this.objectMapper = objectMapper;
    }

    private User getAuthenticatedUserEntity() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UnauthorizedException(ErrorCode.USER_NOT_FOUND, userDetails.getUsername()));
        }
        throw new UnauthorizedException(ErrorCode.AUTH_INVALID);
    }

    @Override
    public PostResponse createPost(PostRequest request) {
        User currentUser = getAuthenticatedUserEntity();
        String userId = currentUser.getId();

        VisibilityStatus visibilityStatus = VisibilityStatus.valueOf(request.getPrivacy().toUpperCase());
        Set<String> authorizedViewerIds = new HashSet<>();
        if (visibilityStatus == VisibilityStatus.PRIVATE) {
            authorizedViewerIds.add(userId);
            if (request.getSharedWithUserIds() != null) {
                for (String sharedId : request.getSharedWithUserIds()) {
                    if (!userRepository.existsById(sharedId)) {
                        throw new BadRequestException(ErrorCode.USER_NOT_FOUND, sharedId);
                    }
                }
                authorizedViewerIds.addAll(request.getSharedWithUserIds());
            }
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setUserEmail(currentUser.getEmail());
        post.setUserName(currentUser.getName());
        post.setContent(request.getContent());
        post.setLocation(request.getLocation());
        post.setTags(request.getTags());
        post.setVisibilityStatus(visibilityStatus);
        post.setAuthorizedViewerIds(authorizedViewerIds);
        post.setActive(true);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);
        post.setLikedByUsers(new ArrayList<>());
        post.setMediaIds(new ArrayList<>());
        Post savedPost = postRepository.save(post);

        if (request.getImages() != null) {
            request.getImages().forEach(file -> {
                Media media = mediaService.uploadMedia(file, MediaType.IMAGE, savedPost.getId());
                savedPost.getMediaIds().add(media.getId());
            });
        }
        if (request.getVideos() != null) {
            request.getVideos().forEach(file -> {
                Media media = mediaService.uploadMedia(file, MediaType.VIDEO, savedPost.getId());
                savedPost.getMediaIds().add(media.getId());
            });
        }
        if (request.getAudios() != null) {
            request.getAudios().forEach(file -> {
                Media media = mediaService.uploadMedia(file, MediaType.AUDIO, savedPost.getId());
                savedPost.getMediaIds().add(media.getId());
            });
        }

        postRepository.save(savedPost);
        return mapToResponse(savedPost, userId);
    }

    @Override
    public Page<PostResponse> getAllPosts(Pageable pageable, String userId) {
        Page<Post> rawPostsPage = postRepository.findByIsActiveTrue(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        List<PostResponse> filteredPosts = rawPostsPage.getContent().stream()
                .filter(post -> canUserViewPost(post, userId))
                .map(post -> mapToResponse(post, userId))
                .collect(Collectors.toList());

        return new PageImpl<>(filteredPosts, pageable, rawPostsPage.getTotalElements());
    }

    private boolean canUserViewPost(Post post, String userId) {
        if (post.getVisibilityStatus() == VisibilityStatus.PUBLIC) return true;
        if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
            if (userId == null) return false;
            return post.getUserId().equals(userId) || post.getAuthorizedViewerIds().contains(userId);
        }
        return false;
    }

    @Override
    public Page<PostResponse> getPostsForCurrentUser(Pageable pageable, String userId) {
        Page<Post> ownedPosts = postRepository.findByUserIdAndIsActiveTrue(userId, pageable);
        Page<Post> sharedPosts = postRepository.findByAuthorizedViewerIdsContainingAndIsActiveTrue(userId, pageable);

        List<Post> combined = Stream.concat(ownedPosts.getContent().stream(), sharedPosts.getContent().stream())
                .distinct()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), combined.size());
        List<PostResponse> pageContent = combined.subList(start, end).stream()
                .map(post -> mapToResponse(post, userId))
                .collect(Collectors.toList());

        return new PageImpl<>(pageContent, pageable, combined.size());
    }

    @Override
    public Page<PostResponse> getPostsByEmail(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Page<Post> posts = postRepository.findByUserIdAndIsActiveTrue(user.getId(), pageable);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final String currentUserId = auth != null && auth.getPrincipal() instanceof UserDetails details
                ? userRepository.findByEmail(details.getUsername()).map(User::getId).orElse(null)
                : null;

        return posts.map(post -> mapToResponse(post, currentUserId));
    }

    @Override
    public Page<PostResponse> getPostsByUserId(String targetUserId, Pageable pageable, String currentUserId) {
        Page<Post> postsPage = postRepository.findByUserIdAndIsActiveTrue(targetUserId, pageable);

        List<PostResponse> filteredPosts = postsPage.getContent().stream()
                .filter(post -> canUserViewPost(post, currentUserId))
                .map(post -> mapToResponse(post, currentUserId))
                .collect(Collectors.toList());

        return new PageImpl<>(filteredPosts, pageable, postsPage.getTotalElements());
    }

    @Override
    public PostResponse getPostById(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND, postId));
        if (!post.isActive()) {
            throw new BadRequestException(ErrorCode.POST_INACTIVE_ACTION, postId);
        }
        if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE &&
                userId != null &&
                !post.getUserId().equals(userId) &&
                !post.getAuthorizedViewerIds().contains(userId)) {
            throw new ForbiddenException(ErrorCode.POST_ACCESS_DENIED);
        }
        return mapToResponse(post, userId);
    }

    @Override
    public PostResponse updatePost(String id, PostRequest request, String deletedMediaUrls, String userId) {
        Post existingPost = postRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND, id));
        if (!existingPost.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.POST_UNAUTHORIZED_UPDATE);
        }

        existingPost.setContent(request.getContent());
        existingPost.setLocation(request.getLocation());
        existingPost.setTags(request.getTags());
        existingPost.setUpdatedAt(LocalDateTime.now());

        // Cập nhật privacy status
        VisibilityStatus newVisibilityStatus = VisibilityStatus.valueOf(request.getPrivacy().toUpperCase());
        existingPost.setVisibilityStatus(newVisibilityStatus);

        // Cập nhật danh sách authorizedViewerIds
        Set<String> authorizedViewerIds = new HashSet<>();
        if (newVisibilityStatus == VisibilityStatus.PRIVATE) {
            authorizedViewerIds.add(userId); // thêm chủ bài
            if (request.getSharedWithUserIds() != null) {
                for (String sharedId : request.getSharedWithUserIds()) {
                    if (!userRepository.existsById(sharedId)) {
                        throw new BadRequestException(ErrorCode.USER_NOT_FOUND, sharedId);
                    }
                }
                authorizedViewerIds.addAll(request.getSharedWithUserIds());
            }
        }
        existingPost.setAuthorizedViewerIds(authorizedViewerIds);

        // Xóa các media cũ nếu cần
        if (deletedMediaUrls != null && !deletedMediaUrls.isEmpty()) {
            try {
                Map<String, List<String>> deletedUrlsMap = objectMapper.readValue(
                        deletedMediaUrls, new TypeReference<Map<String, List<String>>>() {}
                );

                List<String> allToDelete = new ArrayList<>();
                allToDelete.addAll(deletedUrlsMap.getOrDefault("images", Collections.emptyList()));
                allToDelete.addAll(deletedUrlsMap.getOrDefault("videos", Collections.emptyList()));
                allToDelete.addAll(deletedUrlsMap.getOrDefault("audios", Collections.emptyList()));

                for (String mediaUrl : allToDelete) {
                    mediaService.listByPostId(existingPost.getId()).stream()
                            .filter(m -> m.getPath().equals(mediaUrl))
                            .findFirst().ifPresent(m -> {
                                mediaService.deleteMedia(m.getId());
                                existingPost.getMediaIds().remove(m.getId());
                            });
                }
            } catch (JsonProcessingException e) {
                throw new BadRequestException(ErrorCode.POST_UPDATE_INVALID_DELETED_URLS, e.getMessage());
            }
        }

        // Upload thêm media mới
        if (request.getImages() != null) {
            request.getImages().forEach(file -> {
                Media media = mediaService.uploadMedia(file, MediaType.IMAGE, existingPost.getId());
                existingPost.getMediaIds().add(media.getId());
            });
        }
        if (request.getVideos() != null) {
            request.getVideos().forEach(file -> {
                Media media = mediaService.uploadMedia(file, MediaType.VIDEO, existingPost.getId());
                existingPost.getMediaIds().add(media.getId());
            });
        }
        if (request.getAudios() != null) {
            request.getAudios().forEach(file -> {
                Media media = mediaService.uploadMedia(file, MediaType.AUDIO, existingPost.getId());
                existingPost.getMediaIds().add(media.getId());
            });
        }

        postRepository.save(existingPost);
        return mapToResponse(existingPost, userId);
    }


    @Override
    public void deletePost(String id, String userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND, id));
        if (!post.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.POST_UNAUTHORIZED_DELETE);
        }
        if (!post.isActive()) {
            throw new BadRequestException(ErrorCode.POST_INACTIVE_ACTION, id);
        }

        mediaService.listByPostId(post.getId()).forEach(m -> mediaService.deleteMedia(m.getId()));
        post.setActive(false);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    @Override
    public PostResponse toggleLike(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND, postId));
        if (!post.isActive()) {
            throw new BadRequestException(ErrorCode.POST_INACTIVE_ACTION, postId);
        }
        if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE &&
                !post.getUserId().equals(userId) &&
                !post.getAuthorizedViewerIds().contains(userId)) {
            throw new ForbiddenException(ErrorCode.POST_ACCESS_DENIED);
        }

        List<String> likedByUsers = new ArrayList<>(Optional.ofNullable(post.getLikedByUsers()).orElse(new ArrayList<>()));
        if (likedByUsers.contains(userId)) {
            likedByUsers.remove(userId);
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            likedByUsers.add(userId);
            post.setLikeCount(post.getLikeCount() + 1);
        }
        post.setLikedByUsers(likedByUsers);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        return mapToResponse(post, userId);
    }

    private PostResponse mapToResponse(Post post, String currentUserId) {
        UserResponse author = userRepository.findById(post.getUserId()).map(u -> UserResponse.builder()
                .id(u.getId()).name(u.getName()).email(u.getEmail()).avatarUrl(u.getAvatarUrl()).build()).orElse(null);

        List<Media> medias = mediaService.listByPostId(post.getId());
        List<String> imageUrls = medias.stream().filter(m -> m.getType() == MediaType.IMAGE).map(Media::getPath).toList();
        List<String> videoUrls = medias.stream().filter(m -> m.getType() == MediaType.VIDEO).map(Media::getPath).toList();
        List<String> audioUrls = medias.stream().filter(m -> m.getType() == MediaType.AUDIO).map(Media::getPath).toList();

        Page<CommentResponse> commentsPage = commentService.getCommentsByPostId(
                post.getId(),
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        boolean isLikedByCurrentUser = currentUserId != null && post.getLikedByUsers().contains(currentUserId);
        boolean isOwnedByCurrentUser = post.getUserId().equals(currentUserId);

        return PostResponse.builder()
                .id(post.getId())
                .author(author)
                .content(post.getContent())
                .imageUrls(imageUrls)
                .videoUrls(videoUrls)
                .audioUrls(audioUrls)
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
                .isOwnedByCurrentUser(isOwnedByCurrentUser)
                .comments(commentsPage.getContent())
                .build();
    }
}
