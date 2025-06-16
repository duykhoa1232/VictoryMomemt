package com.example.victorymoments.repository;

import com.example.victorymoments.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface PostRepository extends MongoRepository<Post, String> {
    Page<Post> findByUserIdAndIsActiveTrue(String userId, Pageable pageable);

    Page<Post> findByAuthorizedViewerIdsContainingAndIsActiveTrue(String userId, Pageable pageable);

}