package com.example.victorymoments.service;

import com.example.victorymoments.dto.PostRequest;
import com.example.victorymoments.dto.PostResponse;

import java.util.List;

public interface PostService {
    PostResponse createPost(PostRequest request);
    List<PostResponse> getAllPosts();
}