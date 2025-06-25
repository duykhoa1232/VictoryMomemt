package com.example.victorymoments.service.impl;

import com.example.victorymoments.entity.Comment;
import com.example.victorymoments.entity.Post;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.exception.BadRequestException;
import com.example.victorymoments.exception.ErrorCode;
import com.example.victorymoments.exception.NotFoundException;
import com.example.victorymoments.exception.UnauthorizedException;
import com.example.victorymoments.repository.CommentRepository;
import com.example.victorymoments.repository.PostRepository;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.request.CommentRequest;
import com.example.victorymoments.response.CommentResponse;
import com.example.victorymoments.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND, postId));
        if (!post.isActive()) {
            throw new BadRequestException(ErrorCode.POST_INACTIVE_COMMENT, postId);
        }

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userEmail));

        Comment parentComment = null;
        if (request.getParentCommentId() != null && !request.getParentCommentId().isEmpty()) {
            parentComment = commentRepository.findByIdAndIsActiveTrue(request.getParentCommentId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND, request.getParentCommentId()));
            if (!parentComment.getPostId().equals(postId)) {
                throw new BadRequestException(ErrorCode.COMMENT_REPLY_POST_MISMATCH, request.getParentCommentId());
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

        // update replyCount
        if (parentComment == null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postRepository.save(post);
        } else {
            parentComment.setReplyCount(parentComment.getReplyCount() + 1);
            commentRepository.save(parentComment);
        }

        return mapToResponse(comment, currentUser.getAvatarUrl());
    }

    @Override
    public Page<CommentResponse> getCommentsByPostId(String postId, Pageable pageable) {
        postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND, postId));
        Page<Comment> topLevelCommentsPage = commentRepository
                .findByPostIdAndParentCommentIdIsNullAndIsActiveTrue(postId, pageable);

        List<String> userIds = topLevelCommentsPage.getContent().stream().map(Comment::getUserId).distinct().toList();
        Map<String, String> userAvatarMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getAvatarUrl));
        List<CommentResponse> content = topLevelCommentsPage.getContent().stream()
                .map(comment -> mapToResponseWithoutReplies(comment, userAvatarMap.get(comment.getUserId())))
                .toList();

        return new PageImpl<>(content, pageable, topLevelCommentsPage.getTotalElements());
    }


    @Override
    @Transactional
    public CommentResponse updateComment(String commentId, CommentRequest request, String userEmail) {
        Comment commentToUpdate = commentRepository.findByIdAndIsActiveTrue(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND, commentId));
        if (!commentToUpdate.getUserEmail().equals(userEmail)) {
            throw new UnauthorizedException(ErrorCode.COMMENT_UNAUTHORIZED_UPDATE, commentId);
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BadRequestException(ErrorCode.COMMENT_CONTENT_REQUIRED, commentId);
        }
        commentToUpdate.setContent(request.getContent());
        commentToUpdate.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(commentToUpdate);

        User commentUser = userRepository.findById(commentToUpdate.getUserId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, commentToUpdate.getUserId()));
        return mapToResponse(commentToUpdate, commentUser.getAvatarUrl());
    }

    @Override
    @Transactional
    public void deleteComment(String commentId, String userEmail) {
        Comment comment = commentRepository.findByIdAndIsActiveTrue(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND, commentId));
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userEmail));
        if (!comment.getUserEmail().equals(userEmail) && !currentUser.getRoles().contains("ADMIN")) {
            throw new UnauthorizedException(ErrorCode.COMMENT_UNAUTHORIZED_DELETE, commentId);
        }

        comment.setActive(false);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        if (comment.getParentCommentId() == null) {
            Post post = postRepository.findById(comment.getPostId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND, comment.getPostId()));
            post.setCommentCount(post.getCommentCount() - 1);
            postRepository.save(post);
        } else {
            Comment parentComment = commentRepository.findById(comment.getParentCommentId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND, comment.getParentCommentId()));
            parentComment.setReplyCount(parentComment.getReplyCount() - 1);
            commentRepository.save(parentComment);
        }
    }

    private CommentResponse mapToResponse(Comment comment, String userAvatar) {
        return mapToResponseWithoutReplies(comment, userAvatar);
    }

    private CommentResponse mapToResponseWithoutReplies(Comment comment, String userAvatar) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .userEmail(comment.getUserEmail())
                .userName(comment.getUserName())
                .userAvatar(userAvatar)
                .content(comment.getContent())
                .parentCommentId(comment.getParentCommentId())
                .replyCount(comment.getReplyCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isActive(comment.isActive())
                .replies(null)
                .build();
    }
}
