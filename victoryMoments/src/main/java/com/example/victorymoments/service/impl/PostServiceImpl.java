package com.example.victorymoments.service.impl;

import com.example.victorymoments.dto.PostRequest;
import com.example.victorymoments.dto.PostResponse;
import com.example.victorymoments.dto.UserResponse;
import com.example.victorymoments.entity.Post;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.entity.VisibilityStatus;
import com.example.victorymoments.repository.PostRepository;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.service.PostService;
import com.example.victorymoments.service.S3Service;
import com.example.victorymoments.util.SecurityUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;


    private User getCurrentUser() {
        String userEmail = SecurityUtil.getCurrentUsername();
        if (userEmail == null || "anonymousUser".equals(userEmail)) {
            return null;
        }
        return userRepository.findByEmail(userEmail)
                .orElse(null);
    }

    private String getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }


    @Override
    @Transactional
    public PostResponse createPost(PostRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated or user details not found.");
        }
        String userId = currentUser.getId();
        String userEmail = currentUser.getEmail();
        String userName = currentUser.getName();

        List<String> imageUrls = s3Service.uploadFiles(request.getImages(), "posts/images");
        List<String> videoUrls = s3Service.uploadFiles(request.getVideos(), "posts/videos");
        List<String> audioUrls = s3Service.uploadFiles(request.getAudios(), "posts/audios");

        VisibilityStatus visibilityStatus = request.getVisibilityStatus();
        if (visibilityStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visibility status is required.");
        }

        List<String> authorizedViewerIds = new ArrayList<>();
        if (visibilityStatus == VisibilityStatus.PRIVATE) {
            authorizedViewerIds.add(userId);
            if (request.getAuthorizedViewerIds() != null && !request.getAuthorizedViewerIds().isEmpty()) {
                Set<String> uniqueViewers = new HashSet<>(request.getAuthorizedViewerIds());
                uniqueViewers.add(userId);
                authorizedViewerIds.addAll(uniqueViewers);
            }
        }

        Post post = Post.builder()
                .userId(userId)
                .userEmail(userEmail)
                .userName(userName)
                .content(request.getContent())
                .imageUrls(imageUrls)
                .videoUrls(videoUrls)
                .audioUrls(audioUrls)
                .location(request.getLocation())
                .tags(request.getTags() != null ? new ArrayList<>(request.getTags()) : new ArrayList<>())
                .visibilityStatus(visibilityStatus)
                .authorizedViewerIds(authorizedViewerIds)
                .likeCount(0)
                .likedByUsers(new ArrayList<>())
                .commentCount(0)
                .shareCount(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();


        post = postRepository.save(post);
        logger.info("New post created with ID: {} by user {}", post.getId(), userEmail);
        return mapToResponse(post);
    }

    @Override
    public Page<PostResponse> getAllPosts(Pageable pageable, String authenticatedUserEmail) {
        final String finalCurrentUserId;
        if (authenticatedUserEmail != null && !"anonymousUser".equals(authenticatedUserEmail)) {
            User currentUser = userRepository.findByEmail(authenticatedUserEmail).orElse(null);
            finalCurrentUserId = (currentUser != null) ? currentUser.getId() : null;
        } else {
            finalCurrentUserId = null;
        }


        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<Post> rawPostsPage = postRepository.findByIsActiveTrue(sortedPageable);

        List<PostResponse> filteredPosts = rawPostsPage.getContent().stream()
                .filter(post -> {
                    if (post.getVisibilityStatus() == VisibilityStatus.PUBLIC) {
                        return true;
                    }
                    if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
                        if (finalCurrentUserId == null) return false;
                        boolean isOwner = post.getUserId().equals(finalCurrentUserId);
                        boolean isAuthorizedViewer = post.getAuthorizedViewerIds() != null && post.getAuthorizedViewerIds().contains(finalCurrentUserId);
                        return isOwner || isAuthorizedViewer;
                    }

                    return false;
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(filteredPosts, sortedPageable, filteredPosts.size());
    }


    @Override
    public Page<PostResponse> getPostsForCurrentUser(Pageable pageable, String userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
        }

        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);



        Page<Post> authoredPostsPage = postRepository.findByUserIdAndIsActiveTrue(userId, sortedPageable);
        List<Post> authoredPosts = authoredPostsPage.getContent();

        Page<Post> sharedPrivatePostsPage = postRepository.findByAuthorizedViewerIdsContainingAndIsActiveTrue(userId, sortedPageable);
        List<Post> sharedPrivatePosts = sharedPrivatePostsPage.getContent();

        List<Post> combinedPosts = Stream.concat(authoredPosts.stream(), sharedPrivatePosts.stream())
                .distinct()
                .filter(Post::isActive)
                .sorted(Comparator.comparing(Post::getCreatedAt, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        int pageSize = sortedPageable.getPageSize();
        int currentPage = sortedPageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Post> pageContent;

        if (combinedPosts.size() < startItem) {
            pageContent = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, combinedPosts.size());
            pageContent = combinedPosts.subList(startItem, toIndex);
        }

        List<PostResponse> pageResponses = pageContent.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        long totalElements = combinedPosts.size();

        return new PageImpl<>(pageResponses, sortedPageable, totalElements);
    }

    @Override
    @Transactional
    public PostResponse updatePost(String id, PostRequest request, String deletedMediaUrls, String userId) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + id));

        if (userId == null || !existingPost.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own posts.");
        }

        if (!existingPost.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update an inactive post.");
        }

        existingPost.setContent(request.getContent());
        existingPost.setLocation(request.getLocation());
        existingPost.setTags(request.getTags() != null ? new ArrayList<>(request.getTags()) : new ArrayList<>());
        existingPost.setUpdatedAt(LocalDateTime.now());

        existingPost.setVisibilityStatus(request.getVisibilityStatus());
        if (existingPost.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
            List<String> newAuthorizedViewers = new ArrayList<>();
            newAuthorizedViewers.add(userId);
            if (request.getAuthorizedViewerIds() != null && !request.getAuthorizedViewerIds().isEmpty()) {
                Set<String> uniqueViewers = new HashSet<>(request.getAuthorizedViewerIds());
                uniqueViewers.add(userId);
                newAuthorizedViewers.addAll(uniqueViewers);
            }
            existingPost.setAuthorizedViewerIds(newAuthorizedViewers);
        } else {
            existingPost.setAuthorizedViewerIds(new ArrayList<>());
        }

        if (deletedMediaUrls != null && !deletedMediaUrls.isEmpty()) {
            try {
                Map<String, List<String>> deletedUrls = objectMapper.readValue(deletedMediaUrls, new TypeReference<Map<String, List<String>>>() {});

                if (deletedUrls.get("images") != null && !deletedUrls.get("images").isEmpty()) {
                    deleteOldFilesFromS3(deletedUrls.get("images"));
                    List<String> remainingImages = existingPost.getImageUrls() != null
                            ? existingPost.getImageUrls().stream()
                            .filter(url -> !deletedUrls.get("images").contains(url))
                            .collect(Collectors.toList())
                            : new ArrayList<>();
                    existingPost.setImageUrls(remainingImages);
                }
                if (deletedUrls.get("videos") != null && !deletedUrls.get("videos").isEmpty()) {
                    deleteOldFilesFromS3(deletedUrls.get("videos"));
                    List<String> remainingVideos = existingPost.getVideoUrls() != null
                            ? existingPost.getVideoUrls().stream()
                            .filter(url -> !deletedUrls.get("videos").contains(url))
                            .collect(Collectors.toList())
                            : new ArrayList<>();
                    existingPost.setVideoUrls(remainingVideos);
                }
                if (deletedUrls.get("audios") != null && !deletedUrls.get("audios").isEmpty()) {
                    deleteOldFilesFromS3(deletedUrls.get("audios"));
                    List<String> remainingAudios = existingPost.getAudioUrls() != null
                            ? existingPost.getAudioUrls().stream()
                            .filter(url -> !deletedUrls.get("audios").contains(url))
                            .collect(Collectors.toList())
                            : new ArrayList<>();
                    existingPost.setAudioUrls(remainingAudios);
                }
            } catch (JsonProcessingException e) {
                logger.error("Invalid deletedMediaUrls JSON format: {}", deletedMediaUrls, e);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid deletedMediaUrls format.");
            }
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> newImageUrls = s3Service.uploadFiles(request.getImages(), "posts/images");
            List<String> currentImages = existingPost.getImageUrls() != null ? existingPost.getImageUrls() : new ArrayList<>();
            currentImages.addAll(newImageUrls);
            existingPost.setImageUrls(currentImages);
        }

        if (request.getVideos() != null && !request.getVideos().isEmpty()) {
            List<String> newVideoUrls = s3Service.uploadFiles(request.getVideos(), "posts/videos");
            List<String> currentVideos = existingPost.getVideoUrls() != null ? existingPost.getVideoUrls() : new ArrayList<>();
            currentVideos.addAll(newVideoUrls);
            existingPost.setVideoUrls(currentVideos);
        }

        if (request.getAudios() != null && !request.getAudios().isEmpty()) {
            List<String> newAudioUrls = s3Service.uploadFiles(request.getAudios(), "posts/audios");
            List<String> currentAudios = existingPost.getAudioUrls() != null ? existingPost.getAudioUrls() : new ArrayList<>();
            currentAudios.addAll(newAudioUrls);
            existingPost.setAudioUrls(currentAudios);
        }

        existingPost.setUpdatedAt(LocalDateTime.now());
        Post updatedPost = postRepository.save(existingPost);
        logger.info("Post updated with ID: {} by user {}", updatedPost.getId(), userId);
        return mapToResponse(updatedPost);
    }

    @Override
    public void deletePost(String id, String userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + id));

        if (userId == null || !post.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own posts.");
        }

        if (!post.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post with ID: " + id + " is already inactive (soft-deleted).");
        }

        List<String> allFileUrls = new ArrayList<>();
        if (post.getImageUrls() != null) allFileUrls.addAll(post.getImageUrls());
        if (post.getVideoUrls() != null) allFileUrls.addAll(post.getVideoUrls());
        if (post.getAudioUrls() != null) allFileUrls.addAll(post.getAudioUrls());

        deleteOldFilesFromS3(allFileUrls);

        post.setActive(false);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
        logger.info("Post soft-deleted with ID: {} by user {}", post.getId(), userId);
    }

    @Override
    public PostResponse toggleLike(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        if (!post.isActive()) {
            throw new RuntimeException("Cannot like/unlike an inactive post.");
        }

        if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
            if (userId == null || (!post.getUserId().equals(userId) && (post.getAuthorizedViewerIds() == null || !post.getAuthorizedViewerIds().contains(userId)))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You do not have permission to like/unlike this private post.");
            }
        }

        List<String> likedUsers = post.getLikedByUsers();
        if (likedUsers.contains(userId)) {
            likedUsers.remove(userId);
            post.setLikeCount(post.getLikeCount() - 1);
            logger.info("User {} unliked post {}", userId, postId);
        } else {
            likedUsers.add(userId);
            post.setLikeCount(post.getLikeCount() + 1);
            logger.info("User {} liked post {}", userId, postId);
        }
        post.setLikedByUsers(likedUsers);
        post.setUpdatedAt(LocalDateTime.now());
        Post updatedPost = postRepository.save(post);
        return mapToResponse(updatedPost);
    }

    @Override
    public PostResponse getPostById(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + postId));

        if (!post.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post with ID: " + postId + " is inactive.");
        }

        if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
            if (userId == null || (!post.getUserId().equals(userId) && (post.getAuthorizedViewerIds() == null || !post.getAuthorizedViewerIds().contains(userId)))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You do not have permission to view this private post.");
            }
        }

        return mapToResponse(post);
    }


    private void deleteOldFilesFromS3(List<String> fileUrls) {
        if (fileUrls != null && !fileUrls.isEmpty()) {
            for (String url : fileUrls) {
                try {
                    String key = extractS3KeyFromUrl(url);
                    s3Service.deleteFile(key);
                    logger.info("Deleted file from S3: {}", url);
                } catch (Exception e) {
                    logger.error("Error removing file from S3: {}. Error: {}", url, e.getMessage(), e);
                }
            }
        }
    }


    private PostResponse mapToResponse(Post post) {
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

        String currentAuthenticatedUserId = getCurrentUserId();

        boolean isLikedByCurrentUser = currentAuthenticatedUserId != null &&
                post.getLikedByUsers() != null &&
                post.getLikedByUsers().contains(currentAuthenticatedUserId);

        boolean isOwnedByCurrentUser = currentAuthenticatedUserId != null &&
                post.getUserId().equals(currentAuthenticatedUserId);


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
                .isOwnedByCurrentUser(isOwnedByCurrentUser)
                .isSharedPost(false)
                .originalShareId(null)
                .sharedByUserId(null)
                .sharedByUserName(null)
                .sharedContent(null)
                .build();
    }


    private String extractS3KeyFromUrl(String s3Url) {
        try {
            URI uri = new URI(s3Url);
            String path = uri.getPath();
            if (path != null && path.startsWith("/")) {
                return path.substring(1);
            }
        } catch (Exception e) {
            logger.error("Failed to parse S3 URL for key extraction: {}", s3Url, e);
        }
        throw new IllegalArgumentException("Invalid S3 URL format or cannot extract key: " + s3Url);
    }
}