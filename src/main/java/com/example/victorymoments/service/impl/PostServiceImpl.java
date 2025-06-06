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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
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
            throw new RuntimeException("User not authenticated or user details not found.");
        }
        String userId = currentUser.getId();
        String userEmail = currentUser.getEmail();
        String userName = currentUser.getName();

        List<String> imageUrls = s3Service.uploadFiles(request.getImages(), "images");
        List<String> videoUrls = s3Service.uploadFiles(request.getVideos(), "videos");
        List<String> audioUrls = s3Service.uploadFiles(request.getAudios(), "audios");

        VisibilityStatus visibilityStatus = VisibilityStatus.valueOf(request.getPrivacy().toUpperCase());

        Set<String> authorizedViewerIds = new HashSet<>();

//        System.out.println("DEBUG (createPost NEW VERSION): Request Visibility Status: " + visibilityStatus);
//        System.out.println("DEBUG (createPost): Post Owner ID: " + userId);
//        System.out.println("DEBUG (createPost): Initial sharedWithUserIds from request: " + request.getSharedWithUserIds());


        if (visibilityStatus == VisibilityStatus.PRIVATE) {
            authorizedViewerIds.add(userId);
            if (request.getSharedWithUserIds() != null && !request.getSharedWithUserIds().isEmpty()) {
                authorizedViewerIds.addAll(request.getSharedWithUserIds());
            }
        } else {
            authorizedViewerIds.clear();
        }

//        System.out.println("DEBUG (createPost): Final Authorized Viewer IDs for new post: " + authorizedViewerIds);

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
//        System.out.println("DEBUG (createPost): Post saved successfully. Saved Post ID: " + post.getId() + ", Saved Authorized Viewers: " + post.getAuthorizedViewerIds());
        return mapToResponse(post);
    }

    @Override
    public List<PostResponse> getAllPosts() {
        String currentUserId = getCurrentUserId();
//        System.out.println("DEBUG (getAllPosts): User ID making request = " + (currentUserId != null ? currentUserId : "NOT AUTHENTICATED/NULL"));

        return postRepository.findAll().stream()
                .filter(Post::isActive)
                .filter(post -> {
//                    System.out.println("DEBUG (getAllPosts): Processing Post ID: " + post.getId() +
//                            ", Content: '" + post.getContent().substring(0, Math.min(post.getContent().length(), 50)) + "...'" +
//                            ", Visibility: " + post.getVisibilityStatus() +
//                            ", Post Owner ID: " + post.getUserId() +
//                            ", Authorized Viewers: " + post.getAuthorizedViewerIds());

                    if (post.getVisibilityStatus() == VisibilityStatus.PUBLIC) {
//                        System.out.println("DEBUG (getAllPosts): Post ID: " + post.getId() + " is PUBLIC, returning true.");
                        return true;
                    }

                    if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
                        boolean isOwner = (currentUserId != null && post.getUserId().equals(currentUserId));
                        boolean isAuthorizedViewer = (currentUserId != null && post.getAuthorizedViewerIds().contains(currentUserId));

                        boolean shouldReturnPrivatePost = isOwner || isAuthorizedViewer;
//                        System.out.println("DEBUG (getAllPosts): Post ID: " + post.getId() + " is PRIVATE." +
//                                " IsOwner: " + isOwner +
//                                ", IsAuthorizedViewer: " + isAuthorizedViewer +
//                                ", Final decision for PRIVATE post: " + shouldReturnPrivatePost);
                        return shouldReturnPrivatePost;
                    }

//                    System.out.println("DEBUG (getAllPosts): Post ID: " + post.getId() + " has unexpected visibility or is inactive. Returning false.");
                    return false;
                })
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<PostResponse> getPostsForCurrentUser() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("User not authenticated.");
        }

        List<Post> authoredPosts = postRepository.findByUserIdAndIsActiveTrue(currentUserId);
        List<Post> sharedPrivatePosts = postRepository.findByAuthorizedViewerIdsContainingAndIsActiveTrue(currentUserId);
        Set<Post> uniquePosts = new HashSet<>(authoredPosts);
        uniquePosts.addAll(sharedPrivatePosts);

        return uniquePosts.stream()
                .filter(Post::isActive)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PostResponse getPostById(String postId) {
        String currentUserId = getCurrentUserId();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        if (!post.isActive()) {
            throw new RuntimeException("Post with ID: " + postId + " is inactive.");
        }

        if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
            if (currentUserId == null || (!post.getUserId().equals(currentUserId) && !post.getAuthorizedViewerIds().contains(currentUserId))) {
                throw new RuntimeException("Access Denied: You do not have permission to view this private post.");
            }
        }

        return mapToResponse(post);
    }

    @Override
    public PostResponse updatePost(String id, PostRequest request) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        String currentUserId = getCurrentUserId();
        if (currentUserId == null || !existingPost.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized: You can only update your own posts.");
        }

        if (!existingPost.isActive()) {
            throw new RuntimeException("Cannot update an inactive post.");
        }

        existingPost.setContent(request.getContent());
        existingPost.setLocation(request.getLocation());
        existingPost.setTags(request.getTags());
        existingPost.setUpdatedAt(LocalDateTime.now());

        existingPost.setVisibilityStatus(VisibilityStatus.valueOf(request.getPrivacy().toUpperCase()));

        if (existingPost.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
            Set<String> newAuthorizedViewers = new HashSet<>();
            newAuthorizedViewers.add(currentUserId);
            if (request.getSharedWithUserIds() != null && !request.getSharedWithUserIds().isEmpty()) {
                newAuthorizedViewers.addAll(request.getSharedWithUserIds());
            }
            existingPost.setAuthorizedViewerIds(newAuthorizedViewers);
        } else {
            existingPost.setAuthorizedViewerIds(new HashSet<>());
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            deleteOldFilesFromS3(existingPost.getImageUrls());
            List<String> newImageUrls = s3Service.uploadFiles(request.getImages(), "images");
            existingPost.setImageUrls(newImageUrls);
        } else if (request.getImages() != null && request.getImages().isEmpty()) {
            deleteOldFilesFromS3(existingPost.getImageUrls());
            existingPost.setImageUrls(new ArrayList<>());
        }

        if (request.getVideos() != null && !request.getVideos().isEmpty()) {
            deleteOldFilesFromS3(existingPost.getVideoUrls());
            List<String> newVideoUrls = s3Service.uploadFiles(request.getVideos(), "videos");
            existingPost.setVideoUrls(newVideoUrls);
        } else if (request.getVideos() != null && request.getVideos().isEmpty()) {
            deleteOldFilesFromS3(existingPost.getVideoUrls());
            existingPost.setVideoUrls(new ArrayList<>());
        }

        if (request.getAudios() != null && !request.getAudios().isEmpty()) {
            deleteOldFilesFromS3(existingPost.getAudioUrls());
            List<String> newAudioUrls = s3Service.uploadFiles(request.getAudios(), "audios");
            existingPost.setAudioUrls(newAudioUrls);
        } else if (request.getAudios() != null && request.getAudios().isEmpty()) {
            deleteOldFilesFromS3(existingPost.getAudioUrls());
            existingPost.setAudioUrls(new ArrayList<>());
        }

        Post updatedPost = postRepository.save(existingPost);
        return mapToResponse(updatedPost);
    }

    @Override
    public void deletePost(String id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        String currentUserId = getCurrentUserId();
        if (currentUserId == null || !post.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized: You can only delete your own posts.");
        }

        if (!post.isActive()) {
            throw new RuntimeException("Post with ID: " + id + " is already inactive (soft-deleted).");
        }
        List<String> allFileUrls = new ArrayList<>();
        if (post.getImageUrls() != null) {
            allFileUrls.addAll(post.getImageUrls());
        }
        if (post.getVideoUrls() != null) {
            allFileUrls.addAll(post.getVideoUrls());
        }
        if (post.getAudioUrls() != null) {
            allFileUrls.addAll(post.getAudioUrls());
        }

        deleteOldFilesFromS3(allFileUrls);

        post.setActive(false);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    @Override
    public PostResponse toggleLike(String postId, String userEmail) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        if (!post.isActive()) {
            throw new RuntimeException("Cannot like/unlike an inactive post.");
        }

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        String currentUserId = currentUser.getId();

        if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
            if (!post.getUserId().equals(currentUserId) && !post.getAuthorizedViewerIds().contains(currentUserId)) {
                throw new RuntimeException("Access Denied: You do not have permission to like/unlike this private post.");
            }
        }

        List<String> likedUsers = post.getLikedByUsers();
        if (likedUsers.contains(currentUserId)) {
            likedUsers.remove(currentUserId);
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            likedUsers.add(currentUserId);
            post.setLikeCount(post.getLikeCount() + 1);
        }
        post.setLikedByUsers(likedUsers);

        post.setUpdatedAt(LocalDateTime.now());
        Post updatedPost = postRepository.save(post);
        return mapToResponse(updatedPost);
    }

    private void deleteOldFilesFromS3(List<String> fileUrls) {
        if (fileUrls != null && !fileUrls.isEmpty()) {
            for (String url : fileUrls) {
                try {
                    String key = url.substring(url.indexOf(".com/") + 5);
                    s3Service.deleteFile(key);
                } catch (Exception e) {
                    System.err.println("Error remove with S3: " + url + ". Error: " + e.getMessage());
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
}