//package com.example.victorymoments.service.impl;
//
//import com.example.victorymoments.dto.PostRequest;
//import com.example.victorymoments.dto.PostResponse;
//import com.example.victorymoments.dto.UserResponse;
//import com.example.victorymoments.entity.Post;
//import com.example.victorymoments.entity.User;
//import com.example.victorymoments.entity.VisibilityStatus;
//import com.example.victorymoments.repository.PostRepository;
//import com.example.victorymoments.repository.UserRepository;
//import com.example.victorymoments.service.PostService;
//import com.example.victorymoments.service.S3Service;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.*;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//@Service
//@RequiredArgsConstructor
//public class PostServiceImpl implements PostService {
//
//    private final PostRepository postRepository;
//    private final UserRepository userRepository;
//    private final S3Service s3Service;
//
//    private String getCurrentUserId() {
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (principal instanceof UserDetails) {
//            String userEmail = ((UserDetails) principal).getUsername();
//            Optional<User> userOptional = userRepository.findByEmail(userEmail);
//            if (userOptional.isPresent()) {
//                return userOptional.get().getId();
//            }
//        }
//        return null;
//    }
//
//    private User getCurrentUser() {
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (principal instanceof UserDetails) {
//            String userEmail = ((UserDetails) principal).getUsername();
//            return userRepository.findByEmail(userEmail)
//                    .orElse(null);
//        }
//        return null;
//    }
//
//    @Override
//    public PostResponse createPost(PostRequest request) {
//        User currentUser = getCurrentUser();
//        if (currentUser == null) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated or user details not found.");
//        }
//        String userId = currentUser.getId();
//        String userEmail = currentUser.getEmail();
//        String userName = currentUser.getName();
//
//        List<String> imageUrls = s3Service.uploadFiles(request.getImages(), "images");
//        List<String> videoUrls = s3Service.uploadFiles(request.getVideos(), "videos");
//        List<String> audioUrls = s3Service.uploadFiles(request.getAudios(), "audios");
//
//        VisibilityStatus visibilityStatus = VisibilityStatus.valueOf(request.getPrivacy().toUpperCase());
//
//        Set<String> authorizedViewerIds = new HashSet<>();
//        if (visibilityStatus == VisibilityStatus.PRIVATE) {
//            authorizedViewerIds.add(userId);
//            if (request.getSharedWithUserIds() != null && !request.getSharedWithUserIds().isEmpty()) {
//                authorizedViewerIds.addAll(request.getSharedWithUserIds());
//            }
//        }
//
//        Post post = Post.builder()
//                .userId(userId)
//                .userEmail(userEmail)
//                .userName(userName)
//                .content(request.getContent())
//                .imageUrls(imageUrls)
//                .videoUrls(videoUrls)
//                .audioUrls(audioUrls)
//                .location(request.getLocation())
//                .tags(request.getTags())
//                .visibilityStatus(visibilityStatus)
//                .authorizedViewerIds(authorizedViewerIds)
//                .likeCount(0)
//                .likedByUsers(new ArrayList<>())
//                .commentCount(0)
//                .shareCount(0)
//                .isActive(true)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//
//        post = postRepository.save(post);
//        return mapToResponse(post);
//    }
//
//    @Override
//    public Page<PostResponse> getAllPosts(Pageable pageable, String userId) {
//        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt");
//        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
//
//        Page<Post> rawPostsPage = postRepository.findAll(pageable);
//
//        List<PostResponse> filteredPosts = rawPostsPage.getContent().stream()
//                .filter(Post::isActive)
//                .filter(post -> {
//                    if (post.getVisibilityStatus() == VisibilityStatus.PUBLIC) {
//                        return true; // Cho phép truy cập public posts ngay cả khi userId null
//                    }
//                    if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
//                        if (userId == null) return false; // Không cho phép xem private nếu không có userId
//                        boolean isOwner = post.getUserId().equals(userId);
//                        boolean isAuthorizedViewer = post.getAuthorizedViewerIds().contains(userId);
//                        return isOwner || isAuthorizedViewer;
//                    }
//                    return false;
//                })
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//
//        return new PageImpl<>(filteredPosts, sortedPageable, rawPostsPage.getTotalElements());
//    }
//
//    @Override
//    public Page<PostResponse> getPostsForCurrentUser(Pageable pageable, String userId) {
//        if (userId == null) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
//        }
//
//        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt");
//        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
//
//        Page<Post> authoredPostsPage = postRepository.findByUserIdAndIsActiveTrue(userId, pageable);
//        Page<Post> sharedPrivatePostsPage = postRepository.findByAuthorizedViewerIdsContainingAndIsActiveTrue(userId, pageable);
//
//        List<Post> combinedPosts = Stream.concat(authoredPostsPage.getContent().stream(), sharedPrivatePostsPage.getContent().stream())
//                .distinct()
//                .filter(Post::isActive)
//                .collect(Collectors.toList());
//
//        int pageSize = pageable.getPageSize();
//        int currentPage = pageable.getPageNumber();
//        int startItem = currentPage * pageSize;
//        List<Post> pageContent;
//
//        if (combinedPosts.size() < startItem) {
//            pageContent = Collections.emptyList();
//        } else {
//            int toIndex = Math.min(startItem + pageSize, combinedPosts.size());
//            pageContent = combinedPosts.subList(startItem, toIndex);
//        }
//
//        List<PostResponse> pageResponses = pageContent.stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//
//        long totalElements = combinedPosts.size();
//
//        return new PageImpl<>(pageResponses, sortedPageable, totalElements);
//    }
//
//    @Override
//    public PostResponse updatePost(String id, PostRequest request, String deletedMediaUrls, String userId) {
//        Post existingPost = postRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + id));
//
//        if (userId == null || !existingPost.getUserId().equals(userId)) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own posts.");
//        }
//
//        if (!existingPost.isActive()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update an inactive post.");
//        }
//
//        existingPost.setContent(request.getContent());
//        existingPost.setLocation(request.getLocation());
//        existingPost.setTags(request.getTags());
//        existingPost.setUpdatedAt(LocalDateTime.now());
//
//        existingPost.setVisibilityStatus(VisibilityStatus.valueOf(request.getPrivacy().toUpperCase()));
//        if (existingPost.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
//            Set<String> newAuthorizedViewers = new HashSet<>();
//            newAuthorizedViewers.add(userId);
//            if (request.getSharedWithUserIds() != null && !request.getSharedWithUserIds().isEmpty()) {
//                newAuthorizedViewers.addAll(request.getSharedWithUserIds());
//            }
//            existingPost.setAuthorizedViewerIds(newAuthorizedViewers);
//        } else {
//            existingPost.setAuthorizedViewerIds(new HashSet<>());
//        }
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        if (deletedMediaUrls != null && !deletedMediaUrls.isEmpty()) {
//            try {
//                Map<String, List<String>> deletedUrls = objectMapper.readValue(deletedMediaUrls, new TypeReference<Map<String, List<String>>>() {});
//                if (deletedUrls.get("images") != null && !deletedUrls.get("images").isEmpty()) {
//                    deleteOldFilesFromS3(deletedUrls.get("images"));
//                    List<String> remainingImages = existingPost.getImageUrls() != null
//                            ? existingPost.getImageUrls().stream()
//                            .filter(url -> !deletedUrls.get("images").contains(url))
//                            .collect(Collectors.toList())
//                            : new ArrayList<>();
//                    existingPost.setImageUrls(remainingImages);
//                }
//                if (deletedUrls.get("videos") != null && !deletedUrls.get("videos").isEmpty()) {
//                    deleteOldFilesFromS3(deletedUrls.get("videos"));
//                    List<String> remainingVideos = existingPost.getVideoUrls() != null
//                            ? existingPost.getVideoUrls().stream()
//                            .filter(url -> !deletedUrls.get("videos").contains(url))
//                            .collect(Collectors.toList())
//                            : new ArrayList<>();
//                    existingPost.setVideoUrls(remainingVideos);
//                }
//                if (deletedUrls.get("audios") != null && !deletedUrls.get("audios").isEmpty()) {
//                    deleteOldFilesFromS3(deletedUrls.get("audios"));
//                    List<String> remainingAudios = existingPost.getAudioUrls() != null
//                            ? existingPost.getAudioUrls().stream()
//                            .filter(url -> !deletedUrls.get("audios").contains(url))
//                            .collect(Collectors.toList())
//                            : new ArrayList<>();
//                    existingPost.setAudioUrls(remainingAudios);
//                }
//            } catch (JsonProcessingException e) {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid deletedMediaUrls format.");
//            }
//        }
//
//        if (request.getImages() != null && !request.getImages().isEmpty()) {
//            List<String> newImageUrls = s3Service.uploadFiles(request.getImages(), "images");
//            List<String> currentImages = existingPost.getImageUrls() != null ? existingPost.getImageUrls() : new ArrayList<>();
//            currentImages.addAll(newImageUrls);
//            existingPost.setImageUrls(currentImages);
//        }
//
//        if (request.getVideos() != null && !request.getVideos().isEmpty()) {
//            List<String> newVideoUrls = s3Service.uploadFiles(request.getVideos(), "videos");
//            List<String> currentVideos = existingPost.getVideoUrls() != null ? existingPost.getVideoUrls() : new ArrayList<>();
//            currentVideos.addAll(newVideoUrls);
//            existingPost.setVideoUrls(currentVideos);
//        }
//
//        if (request.getAudios() != null && !request.getAudios().isEmpty()) {
//            List<String> newAudioUrls = s3Service.uploadFiles(request.getAudios(), "audios");
//            List<String> currentAudios = existingPost.getAudioUrls() != null ? existingPost.getAudioUrls() : new ArrayList<>();
//            currentAudios.addAll(newAudioUrls);
//            existingPost.setAudioUrls(currentAudios);
//        }
//
//        Post updatedPost = postRepository.save(existingPost);
//        return mapToResponse(updatedPost);
//    }
//
//    @Override
//    public void deletePost(String id, String userId) {
//        Post post = postRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + id));
//
//        if (userId == null || !post.getUserId().equals(userId)) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own posts.");
//        }
//
//        if (!post.isActive()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post with ID: " + id + " is already inactive (soft-deleted).");
//        }
//
//        List<String> allFileUrls = new ArrayList<>();
//        if (post.getImageUrls() != null) allFileUrls.addAll(post.getImageUrls());
//        if (post.getVideoUrls() != null) allFileUrls.addAll(post.getVideoUrls());
//        if (post.getAudioUrls() != null) allFileUrls.addAll(post.getAudioUrls());
//
//        deleteOldFilesFromS3(allFileUrls);
//
//        post.setActive(false);
//        post.setUpdatedAt(LocalDateTime.now());
//        postRepository.save(post);
//    }
//
//    @Override
//    public PostResponse toggleLike(String postId, String userId) {
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
//
//        if (!post.isActive()) {
//            throw new RuntimeException("Cannot like/unlike an inactive post.");
//        }
//
//        if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
//            if (!post.getUserId().equals(userId) && !post.getAuthorizedViewerIds().contains(userId)) {
//                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You do not have permission to like/unlike this private post.");
//            }
//        }
//
//        List<String> likedUsers = post.getLikedByUsers();
//        if (likedUsers.contains(userId)) {
//            likedUsers.remove(userId);
//            post.setLikeCount(post.getLikeCount() - 1);
//        } else {
//            likedUsers.add(userId);
//            post.setLikeCount(post.getLikeCount() + 1);
//        }
//        post.setLikedByUsers(likedUsers);
//        post.setUpdatedAt(LocalDateTime.now());
//        Post updatedPost = postRepository.save(post);
//        return mapToResponse(updatedPost);
//    }
//
//    @Override
//    public PostResponse getPostById(String postId, String userId) {
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + postId));
//
//        if (!post.isActive()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post with ID: " + postId + " is inactive.");
//        }
//
//        if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
//            if (userId == null || (!post.getUserId().equals(userId) && !post.getAuthorizedViewerIds().contains(userId))) {
//                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You do not have permission to view this private post.");
//            }
//        }
//
//        return mapToResponse(post);
//    }
//
//    private void deleteOldFilesFromS3(List<String> fileUrls) {
//        if (fileUrls != null && !fileUrls.isEmpty()) {
//            for (String url : fileUrls) {
//                try {
//                    String key = url.substring(url.indexOf(".com/") + 5);
//                    s3Service.deleteFile(key);
//                } catch (Exception e) {
//                    System.err.println("Error removing file from S3: " + url + ". Error: " + e.getMessage());
//                }
//            }
//        }
//    }
//
//    private PostResponse mapToResponse(Post post) {
//        Optional<User> authorOptional = userRepository.findById(post.getUserId());
//        UserResponse authorResponse = null;
//        if (authorOptional.isPresent()) {
//            User authorEntity = authorOptional.get();
//            authorResponse = UserResponse.builder()
//                    .id(authorEntity.getId())
//                    .name(authorEntity.getName())
//                    .email(authorEntity.getEmail())
//                    .avatarUrl(authorEntity.getAvatarUrl())
//                    .build();
//        }
//
//        boolean isLikedByCurrentUser = post.getLikedByUsers().contains(post.getUserId()); // Sửa logic so sánh
//        boolean isOwnedByCurrentUser = post.getUserId().equals(getCurrentUserId()); // Sửa logic so sánh với userId hiện tại
//
//        return PostResponse.builder()
//                .id(post.getId())
//                .author(authorResponse)
//                .content(post.getContent())
//                .imageUrls(post.getImageUrls())
//                .videoUrls(post.getVideoUrls())
//                .audioUrls(post.getAudioUrls())
//                .location(post.getLocation())
//                .visibilityStatus(post.getVisibilityStatus())
//                .authorizedViewerIds(post.getAuthorizedViewerIds())
//                .tags(post.getTags())
//                .likeCount(post.getLikeCount())
//                .likedByUsers(post.getLikedByUsers())
//                .commentCount(post.getCommentCount())
//                .shareCount(post.getShareCount())
//                .createdAt(post.getCreatedAt())
//                .updatedAt(post.getUpdatedAt())
//                .isActive(post.isActive())
//                .isLikedByCurrentUser(isLikedByCurrentUser)
//                .isOwnedByCurrentUser(isOwnedByCurrentUser)
//                .build();
//    }
//}


package com.example.victorymoments.service.impl;

import com.example.victorymoments.dto.CommentResponse; // Import CommentResponse
import com.example.victorymoments.dto.PostRequest;
import com.example.victorymoments.dto.PostResponse;
import com.example.victorymoments.dto.UserResponse;
import com.example.victorymoments.entity.Post;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.entity.VisibilityStatus;
import com.example.victorymoments.repository.PostRepository;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.service.CommentService; // Import CommentService
import com.example.victorymoments.service.PostService;
import com.example.victorymoments.service.S3Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final CommentService commentService; // <--- THÊM DÒNG NÀY ĐỂ INJECT COMMENT SERVICE

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

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String userEmail = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(userEmail)
                .orElse(null);
        }
        return null;
    }

    @Override
    public PostResponse createPost(PostRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated or user details not found.");
        }
        String userId = currentUser.getId();
        String userEmail = currentUser.getEmail();
        String userName = currentUser.getName();

        List<String> imageUrls = s3Service.uploadFiles(request.getImages(), "images");
        List<String> videoUrls = s3Service.uploadFiles(request.getVideos(), "videos");
        List<String> audioUrls = s3Service.uploadFiles(request.getAudios(), "audios");

        VisibilityStatus visibilityStatus = VisibilityStatus.valueOf(request.getPrivacy().toUpperCase());

        Set<String> authorizedViewerIds = new HashSet<>();
        if (visibilityStatus == VisibilityStatus.PRIVATE) {
            authorizedViewerIds.add(userId);
            if (request.getSharedWithUserIds() != null && !request.getSharedWithUserIds().isEmpty()) {
                authorizedViewerIds.addAll(request.getSharedWithUserIds());
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
            .tags(request.getTags())
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
        return mapToResponse(post, userId); // <--- Truyền userId vào đây
    }

    @Override
    public Page<PostResponse> getAllPosts(Pageable pageable, String userId) {
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<Post> rawPostsPage = postRepository.findAll(pageable);

        List<PostResponse> filteredPosts = rawPostsPage.getContent().stream()
            .filter(Post::isActive)
            .filter(post -> {
                if (post.getVisibilityStatus() == VisibilityStatus.PUBLIC) {
                    return true; // Cho phép truy cập public posts ngay cả khi userId null
                }
                if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
                    if (userId == null) return false; // Không cho phép xem private nếu không có userId
                    boolean isOwner = post.getUserId().equals(userId);
                    boolean isAuthorizedViewer = post.getAuthorizedViewerIds().contains(userId);
                    return isOwner || isAuthorizedViewer;
                }
                return false;
            })
            .map(post -> mapToResponse(post, userId)) // <--- Truyền userId vào đây
            .collect(Collectors.toList());

        return new PageImpl<>(filteredPosts, sortedPageable, rawPostsPage.getTotalElements());
    }

    @Override
    public Page<PostResponse> getPostsForCurrentUser(Pageable pageable, String userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
        }

        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<Post> authoredPostsPage = postRepository.findByUserIdAndIsActiveTrue(userId, pageable);
        Page<Post> sharedPrivatePostsPage = postRepository.findByAuthorizedViewerIdsContainingAndIsActiveTrue(userId, pageable);

        List<Post> combinedPosts = Stream.concat(authoredPostsPage.getContent().stream(), sharedPrivatePostsPage.getContent().stream())
            .distinct()
            .filter(Post::isActive)
            .collect(Collectors.toList());

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Post> pageContent;

        if (combinedPosts.size() < startItem) {
            pageContent = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, combinedPosts.size());
            pageContent = combinedPosts.subList(startItem, toIndex);
        }

        List<PostResponse> pageResponses = pageContent.stream()
            .map(post -> mapToResponse(post, userId)) // <--- Truyền userId vào đây
            .collect(Collectors.toList());

        long totalElements = combinedPosts.size();

        return new PageImpl<>(pageResponses, sortedPageable, totalElements);
    }

    @Override
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
        existingPost.setTags(request.getTags());
        existingPost.setUpdatedAt(LocalDateTime.now());

        existingPost.setVisibilityStatus(VisibilityStatus.valueOf(request.getPrivacy().toUpperCase()));
        if (existingPost.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
            Set<String> newAuthorizedViewers = new HashSet<>();
            newAuthorizedViewers.add(userId);
            if (request.getSharedWithUserIds() != null && !request.getSharedWithUserIds().isEmpty()) {
                newAuthorizedViewers.addAll(request.getSharedWithUserIds());
            }
            existingPost.setAuthorizedViewerIds(newAuthorizedViewers);
        } else {
            existingPost.setAuthorizedViewerIds(new HashSet<>());
        }

        ObjectMapper objectMapper = new ObjectMapper();
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid deletedMediaUrls format.");
            }
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> newImageUrls = s3Service.uploadFiles(request.getImages(), "images");
            List<String> currentImages = existingPost.getImageUrls() != null ? existingPost.getImageUrls() : new ArrayList<>();
            currentImages.addAll(newImageUrls);
            existingPost.setImageUrls(currentImages);
        }

        if (request.getVideos() != null && !request.getVideos().isEmpty()) {
            List<String> newVideoUrls = s3Service.uploadFiles(request.getVideos(), "videos");
            List<String> currentVideos = existingPost.getVideoUrls() != null ? existingPost.getVideoUrls() : new ArrayList<>();
            currentVideos.addAll(newVideoUrls);
            existingPost.setVideoUrls(currentVideos);
        }

        if (request.getAudios() != null && !request.getAudios().isEmpty()) {
            List<String> newAudioUrls = s3Service.uploadFiles(request.getAudios(), "audios");
            List<String> currentAudios = existingPost.getAudioUrls() != null ? existingPost.getAudioUrls() : new ArrayList<>();
            currentAudios.addAll(newAudioUrls);
            existingPost.setAudioUrls(currentAudios);
        }

        Post updatedPost = postRepository.save(existingPost);

        return mapToResponse(updatedPost, userId); // <--- Truyền userId vào đây
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
    }

    @Override
    public PostResponse toggleLike(String postId, String userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        if (!post.isActive()) {
            throw new RuntimeException("Cannot like/unlike an inactive post.");
        }

        if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
            if (!post.getUserId().equals(userId) && !post.getAuthorizedViewerIds().contains(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You do not have permission to like/unlike this private post.");
            }
        }

        List<String> likedUsers = post.getLikedByUsers();
        if (likedUsers.contains(userId)) {
            likedUsers.remove(userId);
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            likedUsers.add(userId);
            post.setLikeCount(post.getLikeCount() + 1);
        }
        post.setLikedByUsers(likedUsers);
        post.setUpdatedAt(LocalDateTime.now());
        Post updatedPost = postRepository.save(post);
        return mapToResponse(updatedPost, userId); // <--- Truyền userId vào đây
    }

    @Override
    public PostResponse getPostById(String postId, String userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + postId));

        if (!post.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post with ID: " + postId + " is inactive.");
        }

        if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
            if (userId == null || (!post.getUserId().equals(userId) && !post.getAuthorizedViewerIds().contains(userId))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You do not have permission to view this private post.");
            }
        }

        return mapToResponse(post, userId); // <--- Truyền userId vào đây
    }

    private void deleteOldFilesFromS3(List<String> fileUrls) {
        if (fileUrls != null && !fileUrls.isEmpty()) {
            for (String url : fileUrls) {
                try {
                    String key = url.substring(url.indexOf(".com/") + 5);
                    s3Service.deleteFile(key);
                } catch (Exception e) {
                    System.err.println("Error removing file from S3: " + url + ". Error: " + e.getMessage());
                }
            }
        }
    }

    // Cập nhật phương thức mapToResponse để nhận userId hiện tại
    private PostResponse mapToResponse(Post post, String currentUserId) { // <--- Thêm tham số currentUserId
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

        boolean isLikedByCurrentUser = false;
        if (currentUserId != null && post.getLikedByUsers() != null) {
            isLikedByCurrentUser = post.getLikedByUsers().contains(currentUserId);
        }
        boolean isOwnedByCurrentUser = post.getUserId().equals(currentUserId);

        // Lấy danh sách comments của bài đăng và ánh xạ vào PostResponse
        // Đây sẽ là một lời gọi đến CommentService của bạn
        List<CommentResponse> comments = commentService.getCommentsByPostId(post.getId());

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
            .comments(comments) // <--- GÁN DANH SÁCH COMMENTS VÀO ĐÂY
            .build();
    }
}
