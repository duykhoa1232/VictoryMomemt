package com.example.victorymoments.service.impl;

import com.example.victorymoments.request.FriendRequest;
import com.example.victorymoments.response.FriendResponse;
import com.example.victorymoments.entity.Friendship;
import com.example.victorymoments.entity.FriendshipStatus;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.repository.FriendshipRepository;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.service.FriendshipService;
import org.springframework.stereotype.Service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.victorymoments.exception.BadRequestException;
import com.example.victorymoments.exception.ConflictException;
import com.example.victorymoments.exception.NotFoundException;
import com.example.victorymoments.exception.UnauthorizedException;
import com.example.victorymoments.exception.ErrorCode;

@Service
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public FriendshipServiceImpl(FriendshipRepository friendshipRepository,
                                 UserRepository userRepository,
                                 MessageSource messageSource) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    @Override
    public FriendResponse sendFriendRequest(String currentUserId, FriendRequest request) {
        String receiverId = request.getReceiverId();

        if (receiverId.equals(currentUserId)) {
            throw new BadRequestException(ErrorCode.FRIENDSHIP_REQUEST_SELF); // không thể gửi kết bạn cho chính mình
        }

        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, currentUserId));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, receiverId));

        Optional<Friendship> existingFriendship = friendshipRepository.findByUsers(currentUserId, receiverId);

        if (existingFriendship.isPresent()) {
            Friendship fs = existingFriendship.get();

            if (fs.getStatus() == FriendshipStatus.PENDING) {
                if (fs.getRequesterId().equals(currentUserId)) {
                    throw new ConflictException(ErrorCode.FRIENDSHIP_REQUEST_PENDING, receiver.getName());
                } else {
                    throw new ConflictException(ErrorCode.FRIENDSHIP_REQUEST_PENDING_FROM_OTHER, sender.getName());
                }
            } else if (fs.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new ConflictException(ErrorCode.FRIENDSHIP_ALREADY_FRIENDS, receiver.getName());
            } else if (fs.getStatus() == FriendshipStatus.DECLINED) {
                friendshipRepository.delete(fs); // reset để tạo request mới
            }
        }

        Friendship friendship = Friendship.builder()
                .requesterId(currentUserId)
                .receiverId(receiverId)
                .status(FriendshipStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return mapToResponse(friendshipRepository.save(friendship), sender, receiver);
    }

    @Override
    public FriendResponse acceptFriendRequest(String requestId, String currentUserId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FRIENDSHIP_REQUEST_NOT_FOUND, requestId));

        if (!friendship.getReceiverId().equals(currentUserId)) {
            throw new UnauthorizedException(ErrorCode.FRIENDSHIP_UNAUTHORIZED_ACCEPT);
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new BadRequestException(ErrorCode.FRIENDSHIP_INVALID_STATUS);
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setUpdatedAt(LocalDateTime.now());
        friendshipRepository.save(friendship);

        User requester = userRepository.findById(friendship.getRequesterId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, friendship.getRequesterId()));
        User receiver = userRepository.findById(friendship.getReceiverId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, friendship.getReceiverId()));

        return mapToResponse(friendship, requester, receiver);
    }

    @Override
    public void declineFriendRequest(String requestId, String currentUserId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FRIENDSHIP_REQUEST_NOT_FOUND, requestId));

        if (!friendship.getReceiverId().equals(currentUserId)) {
            throw new UnauthorizedException(ErrorCode.FRIENDSHIP_UNAUTHORIZED_DECLINE);
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new BadRequestException(ErrorCode.FRIENDSHIP_INVALID_STATUS);
        }

        friendship.setStatus(FriendshipStatus.DECLINED);
        friendship.setUpdatedAt(LocalDateTime.now());
        friendshipRepository.save(friendship);
    }

    @Override
    public void removeFriend(String otherUserId, String currentUserId) {
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, otherUserId));

        Friendship friendship = friendshipRepository.findByUsers(currentUserId, otherUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FRIENDSHIP_REMOVE_NOT_FOUND));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new BadRequestException(ErrorCode.FRIENDSHIP_NOT_FRIENDS, otherUser.getName());
        }

        friendshipRepository.delete(friendship);
    }

    @Override
    public List<FriendResponse> getFriendRequests(String currentUserId) {
        List<Friendship> requests = friendshipRepository.findByReceiverIdAndStatus(currentUserId, FriendshipStatus.PENDING);

        Map<String, User> userMap = userRepository.findAllById(
                requests.stream().map(Friendship::getRequesterId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(User::getId, u -> u));

        return requests.stream().map(f -> mapToResponse(f, userMap.get(f.getRequesterId()), null)).collect(Collectors.toList());
    }

    @Override
    public List<FriendResponse> getFriends(String currentUserId) {
        List<Friendship> friends = friendshipRepository.findAllByUserIdAndStatus(currentUserId, FriendshipStatus.ACCEPTED);

        List<String> relatedUserIds = friends.stream()
                .map(f -> f.getRequesterId().equals(currentUserId) ? f.getReceiverId() : f.getRequesterId())
                .collect(Collectors.toList());

        Map<String, User> userMap = userRepository.findAllById(relatedUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return friends.stream().map(f -> {
            User requester = userMap.get(f.getRequesterId());
            User receiver = userMap.get(f.getReceiverId());
            return mapToResponse(f, requester, receiver);
        }).collect(Collectors.toList());
    }

    private FriendResponse mapToResponse(Friendship f, User requesterUser, User receiverUser) {
        return FriendResponse.builder()
                .id(f.getId())
                .requesterId(f.getRequesterId())
                .receiverId(f.getReceiverId())
                .status(f.getStatus().name())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .requesterName(requesterUser != null ? requesterUser.getName() : null)
                .requesterEmail(requesterUser != null ? requesterUser.getEmail() : null)
                .requesterAvatar(requesterUser != null ? requesterUser.getAvatarUrl() : null)
                .receiverName(receiverUser != null ? receiverUser.getName() : null)
                .receiverEmail(receiverUser != null ? receiverUser.getEmail() : null)
                .receiverAvatar(receiverUser != null ? receiverUser.getAvatarUrl() : null)
                .build();
    }
}
