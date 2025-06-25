package com.example.victorymoments.controller;

import com.example.victorymoments.entity.Media;
import com.example.victorymoments.entity.MediaType;
import com.example.victorymoments.mapper.MediaMapper;
import com.example.victorymoments.response.MediaResponse;
import com.example.victorymoments.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/media")
@CrossOrigin(origins = {"https://shark-calm-externally.ngrok-free.app", "http://localhost:4200"})
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @Operation(summary = "Upload a new media file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Media uploaded successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MediaResponse.class)
                    )),
            @ApiResponse(responseCode = "400",
                    description = "File is empty or invalid",
                    content = @Content)
    })
    @PostMapping("/upload")
    public ResponseEntity<MediaResponse> upload(
            @Parameter(description = "Media file to upload", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Type of the media file", required = true,
                    example = "IMAGE")
            @RequestParam("type") MediaType type,

            @Parameter(description = "Optional post ID to attach media to",
                    example = "post123")
            @RequestParam(value = "postId", required = false) String postId) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Media media = mediaService.uploadMedia(file, type, postId);
        return ResponseEntity.ok(MediaMapper.toResponse(media));
    }

    @Operation(summary = "List all media for a specific post ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "List of media retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MediaResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Post not found or no media for post",
                    content = @Content)
    })
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<MediaResponse>> listByPost(
            @Parameter(description = "ID of the post to list media for", example = "post123")
            @PathVariable String postId) {
        List<MediaResponse> mediaResponses = mediaService.listByPostId(postId).stream()
                .map(MediaMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(mediaResponses);
    }

    @Operation(summary = "Delete a media file by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Media deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404",
                    description = "Media not found",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(
            @Parameter(description = "ID of the media to delete", example = "media123")
            @PathVariable String id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }
}
