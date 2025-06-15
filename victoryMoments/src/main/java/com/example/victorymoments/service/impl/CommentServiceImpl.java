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
import java.util.List;
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
                .content(request.getContent())
                .parentCommentId(request.getParentCommentId())
                .replyCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        comment = commentRepository.save(comment);

        if (parentComment == null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postRepository.save(post);
        } else {
            parentComment.setReplyCount(parentComment.getReplyCount() + 1);
            commentRepository.save(parentComment);
        }

        return mapToResponse(comment);
    }

    @Override
    public List<CommentResponse> getCommentsByPostId(String postId) {
        return commentRepository.findByPostIdAndIsActiveTrue(postId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponse updateComment(String commentId, CommentRequest request, String userEmail) {
        Comment comment = commentRepository.findByIdAndIsActiveTrue(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        if (!comment.getUserEmail().equals(userEmail)) {
            throw new RuntimeException("You are not authorized to update this comment.");
        }

        comment.setContent(request.getContent());
        comment.setUpdatedAt(LocalDateTime.now());
        comment = commentRepository.save(comment);
        return mapToResponse(comment);
    }

    @Override
    @Transactional
    public void deleteComment(String commentId, String userEmail) {
        Comment comment = commentRepository.findByIdAndIsActiveTrue(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        if (!comment.getUserEmail().equals(userEmail) && !currentUser.getRoles().contains("ADMIN")) {
            throw new RuntimeException("You are not authorized to delete this comment.");
        }

        comment.setActive(false);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        if (comment.getParentCommentId() == null) {
            Post post = postRepository.findById(comment.getPostId())
                    .orElseThrow(() -> new RuntimeException("Associated Post not found for comment."));
            post.setCommentCount(post.getCommentCount() - 1);
            postRepository.save(post);
        } else {
            Comment parentComment = commentRepository.findById(comment.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found for reply."));
            parentComment.setReplyCount(parentComment.getReplyCount() - 1);
            commentRepository.save(parentComment);
        }
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .userEmail(comment.getUserEmail())
                .userName(comment.getUserName())
                .content(comment.getContent())
                .parentCommentId(comment.getParentCommentId())
                .replyCount(comment.getReplyCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isActive(comment.isActive())
                .build();
    }
}