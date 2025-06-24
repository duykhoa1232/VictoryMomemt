package com.example.victorymoments.controller;

import com.example.victorymoments.request.FriendRequest;
import com.example.victorymoments.response.FriendResponse;
import com.example.victorymoments.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Send a friend request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request sent successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FriendResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content)
    })
    @PostMapping("/request")
    public ResponseEntity<FriendResponse> sendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Request payload containing the receiver's ID")
            @RequestBody FriendRequest dto) {
        return ResponseEntity.ok(friendshipService.sendFriendRequest(userDetails.getUsername(), dto));
    }

    @Operation(summary = "Accept a friend request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request accepted",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FriendResponse.class))),
            @ApiResponse(responseCode = "404", description = "Friend request not found",
                    content = @Content)
    })
    @PostMapping("/accept/{id}")
    public ResponseEntity<FriendResponse> accept(
            @Parameter(description = "Friend request ID", example = "abc123")
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(id, userDetails.getUsername()));
    }

    @Operation(summary = "Decline a friend request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Friend request declined successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Friend request not found",
                    content = @Content)
    })
    @PostMapping("/decline/{id}")
    public ResponseEntity<Void> decline(
            @Parameter(description = "Friend request ID to decline")
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        friendshipService.declineFriendRequest(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove a friend")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Friend removed successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Friend not found",
                    content = @Content)
    })
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @Parameter(description = "Friend's user ID to remove")
            @PathVariable String friendId,
            @AuthenticationPrincipal UserDetails userDetails) {
        friendshipService.removeFriend(friendId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get friend requests for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of friend requests",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FriendResponse.class))),
    })
    @GetMapping("/requests")
    public ResponseEntity<List<FriendResponse>> getRequests(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(friendshipService.getFriendRequests(userDetails.getUsername()));
    }

    @Operation(summary = "Get list of friends for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of friends",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FriendResponse.class))),
    })
    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriends(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(friendshipService.getFriends(userDetails.getUsername()));
    }
}
