package com.example.victorymoments.repository;

import com.example.victorymoments.entity.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByPostIdAndIsActiveTrue(String postId);
    Optional<Comment> findByIdAndIsActiveTrue(String id);

    List<Comment> findByParentCommentIdAndIsActiveTrue(String parentCommentId);
    List<Comment> findByPostIdAndParentCommentIdIsNullAndIsActiveTrue(String postId);
}
