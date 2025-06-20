package com.example.victorymoments.service;

import com.example.victorymoments.dto.FriendRequest;
import com.example.victorymoments.dto.FriendResponse;

import java.util.List;

public interface FriendshipService {
    FriendResponse sendFriendRequest(String currentUserId, FriendRequest request);

    FriendResponse acceptFriendRequest(String requestId, String currentUserId);

    void declineFriendRequest(String requestId, String currentUserId);

    void removeFriend(String otherUserId, String currentUserId);

    List<FriendResponse> getFriendRequests(String currentUserId);

    List<FriendResponse> getFriends(String currentUserId);
}

