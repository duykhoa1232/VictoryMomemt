package com.example.victorymoments.repository;

import com.example.victorymoments.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    Page<Post> findByUserIdAndIsActiveTrue(String userId, Pageable pageable);

    Page<Post> findByAuthorizedViewerIdsContainingAndIsActiveTrue(String userId, Pageable pageable);

//    List<Post> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(String userId);

    Optional<Post> findByIdAndIsActiveTrue(String id);

    Page<Post> findByIsActiveTrue(Pageable pageable);


}