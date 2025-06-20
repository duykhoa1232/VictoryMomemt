package com.example.victorymoments.controller;

import com.example.victorymoments.dto.FriendRequest;
import com.example.victorymoments.dto.FriendResponse;
import com.example.victorymoments.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/request")
    public ResponseEntity<FriendResponse> sendRequest(@AuthenticationPrincipal UserDetails userDetails,
                                                      @RequestBody FriendRequest dto) {
        return ResponseEntity.ok(friendshipService.sendFriendRequest(userDetails.getUsername(), dto));
    }

    @PostMapping("/accept/{id}")
    public ResponseEntity<FriendResponse> accept(@PathVariable String id,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(id, userDetails.getUsername()));
    }

    @PostMapping("/decline/{id}")
    public ResponseEntity<Void> decline(@PathVariable String id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        friendshipService.declineFriendRequest(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable String friendId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        friendshipService.removeFriend(friendId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendResponse>> getRequests(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(friendshipService.getFriendRequests(userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriends(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(friendshipService.getFriends(userDetails.getUsername()));
    }
}
