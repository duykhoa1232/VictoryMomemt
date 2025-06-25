// src/main/java/com/example/victorymoments/repository/FriendshipRepository.java
package com.example.victorymoments.repository;

import com.example.victorymoments.entity.Friendship;
import com.example.victorymoments.entity.FriendshipStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends MongoRepository<Friendship, String> {
    // Tìm lời mời từ A -> B
    Optional<Friendship> findByRequesterIdAndReceiverId(String requesterId, String receiverId);

    // Tìm lời mời từ B -> A
    Optional<Friendship> findByReceiverIdAndRequesterId(String receiverId, String requesterId);

    // Tìm kiếm mối quan hệ giữa hai người, không quan trọng ai là requester/receiver
    // Sẽ trả về 1 (hoặc 0) kết quả nếu có mối quan hệ duy nhất
    @Query("{$or: [{ $and: [ { 'requesterId': ?0 }, { 'receiverId': ?1 } ] }, " +
            "{ $and: [ { 'requesterId': ?1 }, { 'receiverId': ?0 } ] }]}")
    Optional<Friendship> findByUsers(String userId1, String userId2);

    List<Friendship> findByReceiverIdAndStatus(String receiverId, FriendshipStatus status);

    @Query("{$or: [ { 'requesterId': ?0, 'status': ?1 }, { 'receiverId': ?0, 'status': ?1 } ] }")
    List<Friendship> findAllByUserIdAndStatus(String userId, FriendshipStatus status);
}