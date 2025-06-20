//
//
//
//package com.example.victorymoments.service.impl;
//
//import com.example.victorymoments.dto.CommentResponse; // Import CommentResponse
//import com.example.victorymoments.dto.PostRequest;
//import com.example.victorymoments.dto.PostResponse;
//import com.example.victorymoments.dto.UserResponse;
//import com.example.victorymoments.entity.Post;
//import com.example.victorymoments.entity.User;
//import com.example.victorymoments.entity.VisibilityStatus;
//import com.example.victorymoments.repository.PostRepository;
//import com.example.victorymoments.repository.UserRepository;
//import com.example.victorymoments.service.CommentService; // Import CommentService
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
//    private final CommentService commentService; // <--- THÊM DÒNG NÀY ĐỂ INJECT COMMENT SERVICE
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
//                .orElse(null);
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
//            .userId(userId)
//            .userEmail(userEmail)
//            .userName(userName)
//            .content(request.getContent())
//            .imageUrls(imageUrls)
//            .videoUrls(videoUrls)
//            .audioUrls(audioUrls)
//            .location(request.getLocation())
//            .tags(request.getTags())
//            .visibilityStatus(visibilityStatus)
//            .authorizedViewerIds(authorizedViewerIds)
//            .likeCount(0)
//            .likedByUsers(new ArrayList<>())
//            .commentCount(0)
//            .shareCount(0)
//            .isActive(true)
//            .createdAt(LocalDateTime.now())
//            .updatedAt(LocalDateTime.now())
//            .build();
//
//        post = postRepository.save(post);
//        return mapToResponse(post, userId); // <--- Truyền userId vào đây
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
//            .filter(Post::isActive)
//            .filter(post -> {
//                if (post.getVisibilityStatus() == VisibilityStatus.PUBLIC) {
//                    return true; // Cho phép truy cập public posts ngay cả khi userId null
//                }
//                if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
//                    if (userId == null) return false; // Không cho phép xem private nếu không có userId
//                    boolean isOwner = post.getUserId().equals(userId);
//                    boolean isAuthorizedViewer = post.getAuthorizedViewerIds().contains(userId);
//                    return isOwner || isAuthorizedViewer;
//                }
//                return false;
//            })
//            .map(post -> mapToResponse(post, userId)) // <--- Truyền userId vào đây
//            .collect(Collectors.toList());
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
//            .distinct()
//            .filter(Post::isActive)
//            .collect(Collectors.toList());
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
//            .map(post -> mapToResponse(post, userId)) // <--- Truyền userId vào đây
//            .collect(Collectors.toList());
//
//        long totalElements = combinedPosts.size();
//
//        return new PageImpl<>(pageResponses, sortedPageable, totalElements);
//    }
//
//    @Override
//    public PostResponse updatePost(String id, PostRequest request, String deletedMediaUrls, String userId) {
//        Post existingPost = postRepository.findById(id)
//            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + id));
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
//                        ? existingPost.getImageUrls().stream()
//                        .filter(url -> !deletedUrls.get("images").contains(url))
//                        .collect(Collectors.toList())
//                        : new ArrayList<>();
//                    existingPost.setImageUrls(remainingImages);
//                }
//                if (deletedUrls.get("videos") != null && !deletedUrls.get("videos").isEmpty()) {
//                    deleteOldFilesFromS3(deletedUrls.get("videos"));
//                    List<String> remainingVideos = existingPost.getVideoUrls() != null
//                        ? existingPost.getVideoUrls().stream()
//                        .filter(url -> !deletedUrls.get("videos").contains(url))
//                        .collect(Collectors.toList())
//                        : new ArrayList<>();
//                    existingPost.setVideoUrls(remainingVideos);
//                }
//                if (deletedUrls.get("audios") != null && !deletedUrls.get("audios").isEmpty()) {
//                    deleteOldFilesFromS3(deletedUrls.get("audios"));
//                    List<String> remainingAudios = existingPost.getAudioUrls() != null
//                        ? existingPost.getAudioUrls().stream()
//                        .filter(url -> !deletedUrls.get("audios").contains(url))
//                        .collect(Collectors.toList())
//                        : new ArrayList<>();
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
//
//        return mapToResponse(updatedPost, userId); // <--- Truyền userId vào đây
//    }
//
//    @Override
//    public void deletePost(String id, String userId) {
//        Post post = postRepository.findById(id)
//            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + id));
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
//            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
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
//        return mapToResponse(updatedPost, userId); // <--- Truyền userId vào đây
//    }
//
//    @Override
//    public PostResponse getPostById(String postId, String userId) {
//        Post post = postRepository.findById(postId)
//            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + postId));
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
//        return mapToResponse(post, userId); // <--- Truyền userId vào đây
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
//    // Cập nhật phương thức mapToResponse để nhận userId hiện tại
//    private PostResponse mapToResponse(Post post, String currentUserId) { // <--- Thêm tham số currentUserId
//        Optional<User> authorOptional = userRepository.findById(post.getUserId());
//        UserResponse authorResponse = null;
//        if (authorOptional.isPresent()) {
//            User authorEntity = authorOptional.get();
//            authorResponse = UserResponse.builder()
//                .id(authorEntity.getId())
//                .name(authorEntity.getName())
//                .email(authorEntity.getEmail())
//                .avatarUrl(authorEntity.getAvatarUrl())
//                .build();
//        }
//
//        boolean isLikedByCurrentUser = false;
//        if (currentUserId != null && post.getLikedByUsers() != null) {
//            isLikedByCurrentUser = post.getLikedByUsers().contains(currentUserId);
//        }
//        boolean isOwnedByCurrentUser = post.getUserId().equals(currentUserId);
//
//        // Lấy danh sách comments của bài đăng và ánh xạ vào PostResponse
//        // Đây sẽ là một lời gọi đến CommentService của bạn
//        List<CommentResponse> comments = commentService.getCommentsByPostId(post.getId());
//
//        return PostResponse.builder()
//            .id(post.getId())
//            .author(authorResponse)
//            .content(post.getContent())
//            .imageUrls(post.getImageUrls())
//            .videoUrls(post.getVideoUrls())
//            .audioUrls(post.getAudioUrls())
//            .location(post.getLocation())
//            .visibilityStatus(post.getVisibilityStatus())
//            .authorizedViewerIds(post.getAuthorizedViewerIds())
//            .tags(post.getTags())
//            .likeCount(post.getLikeCount())
//            .likedByUsers(post.getLikedByUsers())
//            .commentCount(post.getCommentCount())
//            .shareCount(post.getShareCount())
//            .createdAt(post.getCreatedAt())
//            .updatedAt(post.getUpdatedAt())
//            .isActive(post.isActive())
//            .isLikedByCurrentUser(isLikedByCurrentUser)
//            .isOwnedByCurrentUser(isOwnedByCurrentUser)
//            .comments(comments) // <--- GÁN DANH SÁCH COMMENTS VÀO ĐÂY
//            .build();
//    }
//}




package com.example.victorymoments.service.impl;

import com.example.victorymoments.dto.CommentResponse; // Import CommentResponse
import com.example.victorymoments.dto.PostRequest;
import com.example.victorymoments.dto.PostResponse;
import com.example.victorymoments.dto.UserResponse;
import com.example.victorymoments.entity.Post;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.entity.VisibilityStatus; // <-- Đảm bảo đây là enum đúng của bạn
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
    private final CommentService commentService;
    private final ObjectMapper objectMapper; // <-- Đảm bảo bạn đã inject ObjectMapper

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
        return mapToResponse(post, userId);
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
                    return true;
                }
                if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
                    if (userId == null) return false;
                    boolean isOwner = post.getUserId().equals(userId);
                    boolean isAuthorizedViewer = post.getAuthorizedViewerIds().contains(userId);
                    return isOwner || isAuthorizedViewer;
                }
                return false;
            })
            .map(post -> mapToResponse(post, userId))
            .collect(Collectors.toList());

        return new PageImpl<>(filteredPosts, sortedPageable, rawPostsPage.getTotalElements()); // Total elements should reflect original page
    }

    @Override
    public Page<PostResponse> getPostsForCurrentUser(Pageable pageable, String userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
        }

        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // Fetch posts where current user is the author
        Page<Post> authoredPostsPage = postRepository.findByUserIdAndIsActiveTrue(userId, pageable);

        // Fetch posts where current user is an authorized viewer for private posts
        // Note: This might require an additional query or a more complex query if you want a single query.
        // For simplicity, fetching separately and combining.
        Page<Post> sharedPrivatePostsPage = postRepository.findByAuthorizedViewerIdsContainingAndIsActiveTrue(userId, pageable);


        List<Post> combinedPosts = Stream.concat(authoredPostsPage.getContent().stream(), sharedPrivatePostsPage.getContent().stream())
            .distinct() // Remove duplicates if a post is both authored and shared
            .filter(Post::isActive) // Ensure only active posts are considered
            .collect(Collectors.toList());

        // Manual pagination for the combined list
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
            .map(post -> mapToResponse(post, userId))
            .collect(Collectors.toList());

        long totalElements = combinedPosts.size(); // Total elements of the *combined and filtered* list

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

        // ObjectMapper objectMapper = new ObjectMapper(); // Already injected as a final field
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

        return mapToResponse(updatedPost, userId);
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
        return mapToResponse(updatedPost, userId);
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

        return mapToResponse(post, userId);
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

    // **** TRIỂN KHAI CÁC PHƯƠNG THỨC MỚI ****

    @Override
    public Page<PostResponse> getPostsByEmail(String userEmail, Pageable pageable, String currentUserId) {
        User targetUser = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + userEmail));

        Page<Post> postsPage = postRepository.findByUserIdAndIsActiveTrue(targetUser.getId(), pageable);

        List<PostResponse> filteredPosts = postsPage.getContent().stream()
            .filter(Post::isActive)
            .filter(post -> {
                if (post.getVisibilityStatus() == VisibilityStatus.PUBLIC) {
                    return true;
                }
                if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
                    if (currentUserId == null) return false; // Not logged in, can't view private
                    boolean isOwner = post.getUserId().equals(currentUserId);
                    boolean isAuthorizedViewer = post.getAuthorizedViewerIds().contains(currentUserId);
                    return isOwner || isAuthorizedViewer;
                }
                return false;
            })
            .map(post -> mapToResponse(post, currentUserId))
            .collect(Collectors.toList());

        // totalElements ở đây nên là tổng số bài *của người dùng mục tiêu* (targetUser) trước khi lọc visibility
        // hoặc là tổng số bài của targetUser sau khi lọc (filteredPosts.size()).
        // Nếu bạn muốn pagination chính xác, cần xem xét lại PostRepository để lấy tổng số public posts + private posts (nếu current user là owner/viewer)
        // Hiện tại, tôi dùng postsPage.getTotalElements() để giữ tổng số bài của tác giả đó.
        return new PageImpl<>(filteredPosts, pageable, postsPage.getTotalElements());
    }

    @Override
    public Page<PostResponse> getPostsByUserId(String targetUserId, Pageable pageable, String currentUserId) {
        User targetUser = userRepository.findById(targetUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + targetUserId));

        Page<Post> postsPage = postRepository.findByUserIdAndIsActiveTrue(targetUserId, pageable);

        List<PostResponse> filteredPosts = postsPage.getContent().stream()
            .filter(Post::isActive)
            .filter(post -> {
                if (post.getVisibilityStatus() == VisibilityStatus.PUBLIC) {
                    return true;
                }
                if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
                    if (currentUserId == null) return false; // Not logged in, can't view private
                    boolean isOwner = post.getUserId().equals(currentUserId);
                    boolean isAuthorizedViewer = post.getAuthorizedViewerIds().contains(currentUserId);
                    return isOwner || isAuthorizedViewer;
                }
                return false;
            })
            .map(post -> mapToResponse(post, currentUserId))
            .collect(Collectors.toList());

        // Tương tự như getPostsByEmail, totalElements ở đây cần xem xét lại tùy thuộc vào yêu cầu pagination.
        return new PageImpl<>(filteredPosts, pageable, postsPage.getTotalElements());
    }

    // Cập nhật phương thức mapToResponse để nhận userId hiện tại
    private PostResponse mapToResponse(Post post, String currentUserId) {
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
            .comments(comments)
            .build();
    }
}
