package com.example.victorymoments.repository;

import com.example.victorymoments.entity.Share;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShareRepository extends MongoRepository<Share, String> {
    Page<Share> findBySharedByUserIdAndIsActiveTrue(String sharedByUserId, Pageable pageable);

    Optional<Share> findByIdAndIsActiveTrue(String id);

//    Optional<Share> findByOriginalPostIdAndSharedByUserIdAndIsActiveTrue(String originalPostId, String sharedByUserId);
//
//    long countByOriginalPostIdAndIsActiveTrue(String originalPostId);
}
