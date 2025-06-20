package com.example.victorymoments.repository;

import com.example.victorymoments.entity.Friendship;
import com.example.victorymoments.entity.FriendshipStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface FriendshipRepository extends MongoRepository<Friendship, String> {
    boolean existsByRequesterIdAndReceiverId(String requesterId, String receiverId);

    List<Friendship> findByReceiverIdAndStatus(String receiverId, FriendshipStatus status);

    @Query("{$or: [ { 'requesterId': ?0 }, { 'receiverId': ?0 } ], status: ?1 }")
    List<Friendship> findAllByUserIdAndStatus(String userId, FriendshipStatus status);
}


