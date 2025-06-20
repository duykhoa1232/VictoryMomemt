package com.example.victorymoments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendResponse {
    private String id;
    private String requesterId;
    private String receiverId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
