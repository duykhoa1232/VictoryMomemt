package com.example.victorymoments.service.impl;

import com.example.victorymoments.dto.PostRequest;
import com.example.victorymoments.dto.PostResponse;
import com.example.victorymoments.entity.Post;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.entity.VisibilityStatus; // Import enum mới
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
import java.util.stream.Collectors; // Import Collectors

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    // Helper method để lấy ID người dùng hiện tại từ SecurityContext
    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String userEmail = ((UserDetails) principal).getUsername();
            Optional<User> userOptional = userRepository.findByEmail(userEmail);
            if (userOptional.isPresent()) {
                return userOptional.get().getId();
            }
        }
        return null; // Hoặc ném ngoại lệ nếu không tìm thấy người dùng
    }

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

        // Xử lý logic private/public
        VisibilityStatus visibilityStatus = request.getVisibilityStatus();
        Set<String> authorizedViewerIds = new HashSet<>();

        if (visibilityStatus == VisibilityStatus.PRIVATE) {
            // Nếu là private, thêm chính tác giả vào danh sách người xem
            authorizedViewerIds.add(userId);
            // Thêm các ID người dùng được chỉ định từ request
            if (request.getSharedWithUserIds() != null) {
                authorizedViewerIds.addAll(request.getSharedWithUserIds());
            }
        } else {
            // Nếu là public, không cần danh sách người xem cụ thể
            visibilityStatus = VisibilityStatus.PUBLIC; // Đảm bảo nếu không gửi lên thì mặc định là PUBLIC
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
                .visibilityStatus(visibilityStatus) // Cập nhật trường này
                .authorizedViewerIds(authorizedViewerIds) // Cập nhật trường này
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
        String currentUserId = getCurrentUserId(); // Lấy ID người dùng hiện tại

        return postRepository.findAll().stream()
                .filter(Post::isActive)
                .filter(post -> {
                    // Nếu là PUBLIC, ai cũng xem được
                    if (post.getVisibilityStatus() == VisibilityStatus.PUBLIC) {
                        return true;
                    }
                    // Nếu là PRIVATE, chỉ tác giả hoặc người được chia sẻ mới xem được
                    if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
                        return post.getUserId().equals(currentUserId) || (currentUserId != null && post.getAuthorizedViewerIds().contains(currentUserId));
                    }
                    return false; // Trường hợp khác (ví dụ: null visibility status)
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

        // Lấy bài viết mà người dùng là tác giả
        List<Post> authoredPosts = postRepository.findByUserIdAndIsActiveTrue(currentUserId);

        // Lấy bài viết riêng tư mà người dùng được chia sẻ
        List<Post> sharedPrivatePosts = postRepository.findByAuthorizedViewerIdsContainingAndIsActiveTrue(currentUserId);

        // Kết hợp và loại bỏ trùng lặp (nếu một bài vừa là của mình vừa được chia sẻ)
        Set<Post> uniquePosts = new HashSet<>(authoredPosts);
        uniquePosts.addAll(sharedPrivatePosts);

        return uniquePosts.stream()
                .filter(Post::isActive) // Đảm bảo chỉ lấy bài viết đang hoạt động
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

        // Kiểm tra quyền xem bài viết
        if (post.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
            if (currentUserId == null || (!post.getUserId().equals(currentUserId) && !post.getAuthorizedViewerIds().contains(currentUserId))) {
                throw new RuntimeException("Access Denied: You do not have permission to view this private post.");
            }
        }
        // Đối với PUBLIC, không cần kiểm tra quyền

        return mapToResponse(post);
    }

    @Override
    public PostResponse updatePost(String id, PostRequest request) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        // Kiểm tra xem người dùng hiện tại có phải là chủ sở hữu bài viết không
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

        // Cập nhật trạng thái hiển thị và danh sách người được chia sẻ
        existingPost.setVisibilityStatus(request.getVisibilityStatus());
        if (request.getVisibilityStatus() == VisibilityStatus.PRIVATE) {
            Set<String> newAuthorizedViewers = new HashSet<>();
            newAuthorizedViewers.add(currentUserId); // Luôn thêm tác giả vào danh sách người xem
            if (request.getSharedWithUserIds() != null) {
                newAuthorizedViewers.addAll(request.getSharedWithUserIds());
            }
            existingPost.setAuthorizedViewerIds(newAuthorizedViewers);
        } else {
            existingPost.setAuthorizedViewerIds(new HashSet<>()); // Xóa danh sách nếu chuyển sang PUBLIC
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
            existingPost.setAudioUrls(new ArrayList<>()); // Fix: audios -> audioUrls
        }

        Post updatedPost = postRepository.save(existingPost);
        return mapToResponse(updatedPost);
    }

    @Override
    public void deletePost(String id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        // Kiểm tra xem người dùng hiện tại có phải là chủ sở hữu bài viết không
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
        if (post.getAudioUrls() != null) { // Fix: getAudios() -> getAudioUrls()
            allFileUrls.addAll(post.getAudioUrls());
        }

        deleteOldFilesFromS3(allFileUrls);

        post.setActive(false);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        // Sau khi soft-delete và xóa file, có thể xóa URL để tránh hiển thị URL không tồn tại
        post.setImageUrls(new ArrayList<>());
        post.setVideoUrls(new ArrayList<>());
        post.setAudioUrls(new ArrayList<>());
        postRepository.save(post); // Lưu lại để cập nhật các URL rỗng
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

        // Kiểm tra quyền xem bài viết trước khi cho phép like/unlike
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
                .visibilityStatus(post.getVisibilityStatus()) // Cập nhật từ privacy
                .authorizedViewerIds(post.getAuthorizedViewerIds()) // Thêm trường này
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