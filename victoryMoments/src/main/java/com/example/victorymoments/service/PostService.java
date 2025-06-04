package com.example.victorymoments.service;

import com.example.victorymoments.dto.PostRequest;
import com.example.victorymoments.dto.PostResponse;

import java.util.List;

public interface PostService {
    PostResponse createPost(PostRequest request);
    List<PostResponse> getAllPosts();
    PostResponse updatePost(String id, PostRequest request);
    void deletePost(String id);
    PostResponse toggleLike(String postId, String userEmail);
}