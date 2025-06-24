package com.example.victorymoments.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "friendships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship {
    @Id
    private String id;

    private String requesterId;
    private String receiverId;

    private FriendshipStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

