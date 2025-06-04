package com.example.victorymoments.repository;

import com.example.victorymoments.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByUserIdAndIsActiveTrue(String userId);
    List<Post> findByAuthorizedViewerIdsContainingAndIsActiveTrue(String userId);
}