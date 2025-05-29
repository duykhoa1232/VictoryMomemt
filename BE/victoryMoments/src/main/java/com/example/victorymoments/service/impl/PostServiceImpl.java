package com.example.victorymoments.service.impl;

import com.example.victorymoments.dto.PostRequest;
import com.example.victorymoments.dto.PostResponse;
import com.example.victorymoments.entity.Post;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.repository.PostRepository;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository; // Để lấy thông tin User từ email

    @Value("${file.upload-dir}")
    private String uploadDir; // Đường dẫn thư mục upload, đọc từ application.properties

    @Override
    public PostResponse createPost(PostRequest request) {
        // Lấy thông tin user từ SecurityContextHolder
        String userEmail = null;
        String userId = null;
        String userName = null;

        // Đảm bảo người dùng đã xác thực
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            userEmail = ((UserDetails) principal).getUsername(); // Trong trường hợp này, username là email của user
            Optional<User> userOptional = userRepository.findByEmail(userEmail);
            if (userOptional.isPresent()) {
                User currentUser = userOptional.get();
                userId = currentUser.getId();
                userName = currentUser.getName(); // Giả sử User entity có trường 'name'
            }
        }

        // Nếu không tìm thấy thông tin user hoặc chưa xác thực
        if (userId == null || userEmail == null || userName == null) {
            throw new RuntimeException("User not authenticated or user details not found.");
        }

        List<String> imageUrls = saveFiles(request.getImages(), "images");
        List<String> videoUrls = saveFiles(request.getVideos(), "videos");
        List<String> audioUrls = saveFiles(request.getAudios(), "audios");

        Post post = Post.builder()
                .userId(userId)
                .userEmail(userEmail)
                .userName(userName)
                .content(request.getContent())
                .imageUrls(imageUrls)
                .videoUrls(videoUrls)
                .audioUrls(audioUrls)
                .location(request.getLocation())
                .privacy(request.getPrivacy())
                .tags(request.getTags())
                .likeCount(0) // Khởi tạo giá trị mặc định
                .commentCount(0) // Khởi tạo giá trị mặc định
                .shareCount(0) // Khởi tạo giá trị mặc định
                .isActive(true) // Khởi tạo giá trị mặc định
                .createdAt(LocalDateTime.now()) // @CreatedDate sẽ tự động điền, nhưng có thể khởi tạo ở đây cũng được
                .updatedAt(LocalDateTime.now()) // @LastModifiedDate sẽ tự động điền
                .build();

        post = postRepository.save(post); // Lưu post vào MongoDB

        return mapToResponse(post);
    }

    @Override
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Phương thức lưu file
    private List<String> saveFiles(List<MultipartFile> files, String type) {
        List<String> urls = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            Path typeUploadPath = Paths.get(uploadDir, type).toAbsolutePath().normalize();

            try {
                // Tạo các thư mục nếu chúng chưa tồn tại (ví dụ: uploads/images)
                Files.createDirectories(typeUploadPath);
            } catch (IOException e) {
                System.err.println("Không thể tạo thư mục: " + typeUploadPath + ". Lỗi: " + e.getMessage());
                e.printStackTrace();
                return urls; // Trả về danh sách rỗng nếu không thể tạo thư mục
            }

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue; // Bỏ qua file rỗng
                }
                try {
                    String originalFileName = file.getOriginalFilename();
                    String fileExtension = "";
                    if (originalFileName != null && originalFileName.contains(".")) {
                        fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                    }
                    String fileName = UUID.randomUUID().toString() + fileExtension; // Tên file duy nhất

                    Path destinationFilePath = typeUploadPath.resolve(fileName);

                    file.transferTo(destinationFilePath); // Ghi file vào ổ đĩa

                    // URL để truy cập file từ frontend
                    urls.add("/media/" + type + "/" + fileName);
                } catch (IOException e) {
                    System.err.println("Không thể lưu file: " + file.getOriginalFilename() + ". Lỗi: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return urls;
    }

    // Phương thức map Post entity sang PostResponse DTO
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
                .commentCount(post.getCommentCount())
                .shareCount(post.getShareCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isActive(post.isActive())
                .build();
    }
}