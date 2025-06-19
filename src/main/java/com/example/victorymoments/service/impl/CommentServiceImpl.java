//package com.example.victorymoments.service.impl;
//
//import com.example.victorymoments.dto.CommentRequest;
//import com.example.victorymoments.dto.CommentResponse;
//import com.example.victorymoments.entity.Comment;
//import com.example.victorymoments.entity.Post;
//import com.example.victorymoments.entity.User;
//import com.example.victorymoments.repository.CommentRepository;
//import com.example.victorymoments.repository.PostRepository;
//import com.example.victorymoments.repository.UserRepository;
//import com.example.victorymoments.service.CommentService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//
//@Service
//@RequiredArgsConstructor
//public class CommentServiceImpl implements CommentService {
//
//    private final CommentRepository commentRepository;
//    private final PostRepository postRepository;
//    private final UserRepository userRepository;
//
//
//    @Override
//    @Transactional
//    public CommentResponse createComment(String postId, CommentRequest request, String userEmail) {
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
//        if (!post.isActive()) {
//            throw new RuntimeException("Cannot comment on an inactive post.");
//        }
//
//        User currentUser = userRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
//
//        Comment parentComment = null;
//        if (request.getParentCommentId() != null && !request.getParentCommentId().isEmpty()) {
//            parentComment = commentRepository.findByIdAndIsActiveTrue(request.getParentCommentId())
//                    .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + request.getParentCommentId()));
//            if (!parentComment.getPostId().equals(postId)) {
//                throw new RuntimeException("Reply comment must belong to the same post as its parent.");
//            }
//        }
//
//        Comment comment = Comment.builder()
//                .postId(postId)
//                .userId(currentUser.getId())
//                .userEmail(currentUser.getEmail())
//                .userName(currentUser.getName())
//                .content(request.getContent())
//                .parentCommentId(request.getParentCommentId())
//                .replyCount(0)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .isActive(true)
//                .build();
//
//        comment = commentRepository.save(comment);
//
//        if (parentComment == null) {
//            post.setCommentCount(post.getCommentCount() + 1);
//            postRepository.save(post);
//        } else {
//            parentComment.setReplyCount(parentComment.getReplyCount() + 1);
//            commentRepository.save(parentComment);
//        }
//
//        return mapToResponse(comment);
//    }
//
//    @Override
//    public List<CommentResponse> getCommentsByPostId(String postId) {
//        return commentRepository.findByPostIdAndIsActiveTrue(postId).stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public CommentResponse updateComment(String commentId, CommentRequest request, String userEmail) {
//        Comment comment = commentRepository.findByIdAndIsActiveTrue(commentId)
//                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
//
//        if (!comment.getUserEmail().equals(userEmail)) {
//            throw new RuntimeException("You are not authorized to update this comment.");
//        }
//
//        comment.setContent(request.getContent());
//        comment.setUpdatedAt(LocalDateTime.now());
//        comment = commentRepository.save(comment);
//        return mapToResponse(comment);
//    }
//
//    @Override
//    @Transactional
//    public void deleteComment(String commentId, String userEmail) {
//        Comment comment = commentRepository.findByIdAndIsActiveTrue(commentId)
//                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
//
//        User currentUser = userRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
//
//        if (!comment.getUserEmail().equals(userEmail) && !currentUser.getRoles().contains("ADMIN")) {
//            throw new RuntimeException("You are not authorized to delete this comment.");
//        }
//
//        comment.setActive(false);
//        comment.setUpdatedAt(LocalDateTime.now());
//        commentRepository.save(comment);
//
//        if (comment.getParentCommentId() == null) {
//            Post post = postRepository.findById(comment.getPostId())
//                    .orElseThrow(() -> new RuntimeException("Associated Post not found for comment."));
//            post.setCommentCount(post.getCommentCount() - 1);
//            postRepository.save(post);
//        } else {
//            Comment parentComment = commentRepository.findById(comment.getParentCommentId())
//                    .orElseThrow(() -> new RuntimeException("Parent comment not found for reply."));
//            parentComment.setReplyCount(parentComment.getReplyCount() - 1);
//            commentRepository.save(parentComment);
//        }
//    }
//
//    private CommentResponse mapToResponse(Comment comment) {
//        return CommentResponse.builder()
//                .id(comment.getId())
//                .postId(comment.getPostId())
//                .userId(comment.getUserId())
//                .userEmail(comment.getUserEmail())
//                .userName(comment.getUserName())
//                .content(comment.getContent())
//                .parentCommentId(comment.getParentCommentId())
//                .replyCount(comment.getReplyCount())
//                .createdAt(comment.getCreatedAt())
//                .updatedAt(comment.getUpdatedAt())
//                .isActive(comment.isActive())
//                .build();
//    }
//}

package com.example.victorymoments.service.impl;

import com.example.victorymoments.dto.CommentRequest;
import com.example.victorymoments.dto.CommentResponse;
import com.example.victorymoments.entity.Comment;
import com.example.victorymoments.entity.Post;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.repository.CommentRepository;
import com.example.victorymoments.repository.PostRepository;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public CommentResponse createComment(String postId, CommentRequest request, String userEmail) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        if (!post.isActive()) {
            throw new RuntimeException("Cannot comment on an inactive post.");
        }

        User currentUser = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        Comment parentComment = null;
        if (request.getParentCommentId() != null && !request.getParentCommentId().isEmpty()) {
            parentComment = commentRepository.findByIdAndIsActiveTrue(request.getParentCommentId())
                .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + request.getParentCommentId()));
            if (!parentComment.getPostId().equals(postId)) {
                throw new RuntimeException("Reply comment must belong to the same post as its parent.");
            }
        }

        Comment comment = Comment.builder()
            .postId(postId)
            .userId(currentUser.getId())
            .userEmail(currentUser.getEmail())
            .userName(currentUser.getName())
            // userAvatar (Nếu có)
            .content(request.getContent())
            .parentCommentId(request.getParentCommentId())
            .replyCount(0) // Mặc định là 0 khi tạo mới
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .isActive(true)
            .build();

        comment = commentRepository.save(comment);

        if (parentComment == null) {
            // Đây là bình luận gốc
            post.setCommentCount(post.getCommentCount() + 1);
            postRepository.save(post);
        } else {
            // Đây là phản hồi, cập nhật replyCount của bình luận cha
            parentComment.setReplyCount(parentComment.getReplyCount() + 1);
            commentRepository.save(parentComment);
        }

        // Sau khi tạo bình luận, trả về toàn bộ cây bình luận của post đó
        // (Đây là cách tốt nhất để frontend cập nhật UI một cách nhất quán)
        // Tuy nhiên, vì phương thức createComment trả về CommentResponse đơn lẻ,
        // chúng ta sẽ phải điều chỉnh. Để phù hợp với chữ ký hiện tại,
        // chúng ta sẽ map comment vừa tạo, và sẽ cần frontend để gọi lại API getCommentsByPostId nếu cần cập nhật toàn bộ cây.
        // NHƯNG, VỚI LỖI CẬP NHẬT, CHÚNG TA CẦN CHẮC CHẮN UPDATE TRẢ VỀ ĐỦ DỮ LIỆU.
        // Tôi sẽ sửa lỗi ở Update, CreateComment hiện tại vẫn ổn với logic này nếu Frontend xử lý việc thêm vào mảng.
        return mapToResponse(comment);
    }

    @Override
    public List<CommentResponse> getCommentsByPostId(String postId) {
        List<Comment> allComments = commentRepository.findByPostIdAndIsActiveTrue(postId);

        // Map để dễ dàng truy cập bình luận theo ID
        Map<String, CommentResponse> commentMap = allComments.stream()
            .map(this::mapToResponseWithoutReplies) // Sử dụng phương thức map mới không thêm replies đệ quy ở đây
            .collect(Collectors.toMap(CommentResponse::getId, comment -> comment));

        List<CommentResponse> topLevelComments = new java.util.ArrayList<>();

        // Duyệt qua tất cả bình luận để xây dựng cây
        for (CommentResponse comment : commentMap.values()) {
            if (comment.getParentCommentId() == null || comment.getParentCommentId().isEmpty()) {
                // Đây là bình luận cấp cao nhất
                topLevelComments.add(comment);
            } else {
                // Đây là phản hồi, thêm nó vào danh sách replies của bình luận cha
                CommentResponse parent = commentMap.get(comment.getParentCommentId());
                if (parent != null) {
                    if (parent.getReplies() == null) {
                        parent.setReplies(new java.util.ArrayList<>());
                    }
                    parent.getReplies().add(comment);
                }
            }
        }

        // Sắp xếp các bình luận cấp cao nhất (mới nhất lên đầu)
        topLevelComments.sort(Comparator.comparing(CommentResponse::getCreatedAt).reversed());
        topLevelComments.forEach(this::sortRepliesRecursively); // Sắp xếp các phản hồi đệ quy

        return topLevelComments;
    }

    // Phương thức helper để sắp xếp các phản hồi đệ quy (cũ nhất lên đầu cho replies)
    private void sortRepliesRecursively(CommentResponse comment) {
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            comment.getReplies().sort(Comparator.comparing(CommentResponse::getCreatedAt)); // Cũ nhất lên đầu
            comment.getReplies().forEach(this::sortRepliesRecursively);
        }
    }

    @Override
    @Transactional
    public CommentResponse updateComment(String commentId, CommentRequest request, String userEmail) {
        Comment commentToUpdate = commentRepository.findByIdAndIsActiveTrue(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        if (!commentToUpdate.getUserEmail().equals(userEmail)) {
            throw new RuntimeException("You are not authorized to update this comment.");
        }

        commentToUpdate.setContent(request.getContent());
        commentToUpdate.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(commentToUpdate); // Lưu bình luận đã cập nhật

        // --- BẮT ĐẦU SỬA LỖI Ở ĐÂY ---
        // Vấn đề là `mapToResponse(commentToUpdate)` không bao gồm `replies`.
        // Để trả về comment với replies, chúng ta cần lấy lại toàn bộ cây bình luận
        // của bài đăng chứa comment này, và tìm đúng comment cha để trả về.

        // Bước 1: Tìm ID của bài đăng chứa bình luận được cập nhật
        String postId = commentToUpdate.getPostId();

        // Bước 2: Lấy lại toàn bộ cây bình luận cho bài đăng này
        List<CommentResponse> updatedCommentsTree = getCommentsByPostId(postId);

        // Bước 3: Tìm bình luận đã được cập nhật trong cây và trả về nó.
        // Đây có thể là bình luận gốc hoặc một reply.
        // Chúng ta cần một phương thức đệ quy để tìm trong cây.
        CommentResponse updatedCommentInTree = findCommentInTree(updatedCommentsTree, commentId);

        if (updatedCommentInTree == null) {
            // Trường hợp lỗi: không tìm thấy comment vừa cập nhật trong cây mới.
            // Điều này khó xảy ra nếu logic getCommentsByPostId đúng.
            throw new RuntimeException("Updated comment not found in the reconstructed comment tree.");
        }
        return updatedCommentInTree;
        // --- KẾT THÚC SỬA LỖI ---
    }

    // Phương thức helper để tìm một CommentResponse trong cây bình luận
    private CommentResponse findCommentInTree(List<CommentResponse> comments, String commentIdToFind) {
        if (comments == null) {
            return null;
        }
        for (CommentResponse comment : comments) {
            if (comment.getId().equals(commentIdToFind)) {
                return comment;
            }
            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                CommentResponse foundInReplies = findCommentInTree(comment.getReplies(), commentIdToFind);
                if (foundInReplies != null) {
                    return foundInReplies;
                }
            }
        }
        return null;
    }


    @Override
    @Transactional
    public void deleteComment(String commentId, String userEmail) {
        Comment comment = commentRepository.findByIdAndIsActiveTrue(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        User currentUser = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        // Kiểm tra quyền: người tạo bình luận hoặc ADMIN
        if (!comment.getUserEmail().equals(userEmail) && !currentUser.getRoles().contains("ADMIN")) {
            throw new RuntimeException("You are not authorized to delete this comment.");
        }

        comment.setActive(false); // Đánh dấu là không hoạt động (soft delete)
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        if (comment.getParentCommentId() == null) {
            // Nếu là bình luận gốc, giảm commentCount của bài đăng
            Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new RuntimeException("Associated Post not found for comment."));
            post.setCommentCount(post.getCommentCount() - 1);
            postRepository.save(post);
        } else {
            // Nếu là phản hồi, giảm replyCount của bình luận cha
            Comment parentComment = commentRepository.findById(comment.getParentCommentId())
                .orElseThrow(() -> new RuntimeException("Parent comment not found for reply."));
            parentComment.setReplyCount(parentComment.getReplyCount() - 1);
            commentRepository.save(parentComment);
        }
    }

    // Phương thức này chỉ ánh xạ comment entity thành DTO mà KHÔNG BAO GỒM replies
    // Vì replies sẽ được xây dựng trong getCommentsByPostId()
    private CommentResponse mapToResponseWithoutReplies(Comment comment) {
        return CommentResponse.builder()
            .id(comment.getId())
            .postId(comment.getPostId())
            .userId(comment.getUserId())
            .userEmail(comment.getUserEmail())
            .userName(comment.getUserName())
            // .userAvatar(comment.getUserAvatar()) // Bỏ comment này nếu bạn không có trường userAvatar trong entity Comment
            .content(comment.getContent())
            .parentCommentId(comment.getParentCommentId())
            .replyCount(comment.getReplyCount())
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getUpdatedAt())
            .isActive(comment.isActive())
            .replies(null) // Đảm bảo replies là null hoặc rỗng khi map một bình luận đơn lẻ
            .build();
    }

    // Giữ phương thức mapToResponse này nếu bạn muốn có một cách map bao gồm replies khi cần
    // Tuy nhiên, trong context này, chúng ta sẽ xây dựng replies trong getCommentsByPostId
    // và không cần mapToResponse gán replies từ entity
    private CommentResponse mapToResponse(Comment comment) {
        return mapToResponseWithoutReplies(comment); // Sử dụng lại phương thức không kèm replies
    }
}
