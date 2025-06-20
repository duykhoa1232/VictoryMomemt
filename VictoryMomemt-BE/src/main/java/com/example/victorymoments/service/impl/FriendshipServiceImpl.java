package com.example.victorymoments.service.impl;

import com.example.victorymoments.dto.FriendRequest;
import com.example.victorymoments.dto.FriendResponse;
import com.example.victorymoments.entity.Friendship;
import com.example.victorymoments.entity.FriendshipStatus;
import com.example.victorymoments.repository.FriendshipRepository;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Override
    public FriendResponse sendFriendRequest(String currentUserId, FriendRequest request) {
        String receiverId = request.getReceiverId();

        if (receiverId.equals(currentUserId)) {
            throw new RuntimeException("Bạn không thể gửi lời mời cho chính mình.");
        }

        if (!userRepository.existsById(receiverId)) {
            throw new RuntimeException("Người nhận không tồn tại.");
        }

        if (friendshipRepository.existsByRequesterIdAndReceiverId(currentUserId, receiverId)) {
            throw new RuntimeException("Đã gửi lời mời kết bạn trước đó.");
        }

        Friendship friendship = Friendship.builder()
                .requesterId(currentUserId)
                .receiverId(receiverId)
                .status(FriendshipStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        friendship = friendshipRepository.save(friendship);
        return mapToResponse(friendship);
    }

    @Override
    public FriendResponse acceptFriendRequest(String requestId, String currentUserId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Lời mời không tồn tại."));

        if (!friendship.getReceiverId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền chấp nhận lời mời này.");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(friendshipRepository.save(friendship));
    }

    @Override
    public void declineFriendRequest(String requestId, String currentUserId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Lời mời không tồn tại."));

        if (!friendship.getReceiverId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền từ chối lời mời này.");
        }

        friendship.setStatus(FriendshipStatus.DECLINED);
        friendship.setUpdatedAt(LocalDateTime.now());
        friendshipRepository.save(friendship);
    }

    @Override
    public void removeFriend(String otherUserId, String currentUserId) {
        List<Friendship> friendships = friendshipRepository.findAllByUserIdAndStatus(currentUserId, FriendshipStatus.ACCEPTED);
        for (Friendship f : friendships) {
            if ((f.getRequesterId().equals(currentUserId) && f.getReceiverId().equals(otherUserId)) ||
                    (f.getRequesterId().equals(otherUserId) && f.getReceiverId().equals(currentUserId))) {
                friendshipRepository.delete(f);
                return;
            }
        }
        throw new RuntimeException("Không tìm thấy bạn bè để hủy.");
    }

    @Override
    public List<FriendResponse> getFriendRequests(String currentUserId) {
        return friendshipRepository.findByReceiverIdAndStatus(currentUserId, FriendshipStatus.PENDING)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<FriendResponse> getFriends(String currentUserId) {
        return friendshipRepository.findAllByUserIdAndStatus(currentUserId, FriendshipStatus.ACCEPTED)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private FriendResponse mapToResponse(Friendship f) {
        return FriendResponse.builder()
                .id(f.getId())
                .requesterId(f.getRequesterId())
                .receiverId(f.getReceiverId())
                .status(f.getStatus().name())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}

