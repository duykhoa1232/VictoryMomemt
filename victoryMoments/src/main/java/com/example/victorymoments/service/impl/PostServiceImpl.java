package com.example.victorymoments.service.impl;

import com.example.victorymoments.dto.PostRequest;
import com.example.victorymoments.dto.PostResponse;
import com.example.victorymoments.entity.Post;
import com.example.victorymoments.entity.User;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    public PostResponse createPost(PostRequest request) {
        String userEmail = null;
        String userId = null;
        String userName = null;

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            userEmail = ((UserDetails) principal).getUsername();
            Optional<User> userOptional = userRepository.findByEmail(userEmail);
            if (userOptional.isPresent()) {
                User currentUser = userOptional.get();
                userId = currentUser.getId();
                userName = currentUser.getName();
            }
        }

        if (userId == null || userEmail == null || userName == null) {
            throw new RuntimeException("User not authenticated or user details not found.");
        }

        List<String> imageUrls = s3Service.uploadFiles(request.getImages(), "images");
        List<String> videoUrls = s3Service.uploadFiles(request.getVideos(), "videos");
        List<String> audioUrls = s3Service.uploadFiles(request.getAudios(), "audios");

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
                .likeCount(0)
                .likedByUsers(new ArrayList<>())
                .commentCount(0)
                .shareCount(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        post = postRepository.save(post);
        return mapToResponse(post);
    }

    @Override
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .filter(Post::isActive)
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public PostResponse updatePost(String id, PostRequest request) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        if (!existingPost.isActive()) {
            throw new RuntimeException("Cannot update an inactive post.");
        }

        existingPost.setContent(request.getContent());
        existingPost.setLocation(request.getLocation());
        existingPost.setTags(request.getTags());
        existingPost.setUpdatedAt(LocalDateTime.now());

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

        post.setImageUrls(new ArrayList<>());
        post.setVideoUrls(new ArrayList<>());
        post.setAudioUrls(new ArrayList<>());
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
                    System.err.println("Lỗi khi xóa file S3: " + url + ". Lỗi: " + e.getMessage());
                }
            }
        }
    }


    private PostResponse mapToResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .userEmail(post.getUserEmail())
                .userName(post.getUserName())
                .content(post.getContent())
                .imageUrls(post.getImageUrls())
                .videoUrls(post.getVideoUrls())
                .audioUrls(post.getAudioUrls())
                .location(post.getLocation())
                .privacy(post.getPrivacy())
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