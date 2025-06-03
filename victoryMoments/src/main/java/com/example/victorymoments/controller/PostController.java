package com.example.victorymoments.controller;

import com.example.victorymoments.dto.PostRequest;
import com.example.victorymoments.dto.PostResponse;
import com.example.victorymoments.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @ModelAttribute PostRequest request) {
        return ResponseEntity.ok(postService.createPost(request));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> list() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String id,
            @ModelAttribute PostRequest request) {
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
