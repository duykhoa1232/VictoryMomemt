//package com.example.victorymoments.service;
//
//import com.example.victorymoments.dto.PostRequest;
//import com.example.victorymoments.dto.PostResponse;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//
//
//public interface PostService {
//    PostResponse createPost(PostRequest request);
//
//    Page<PostResponse> getAllPosts(Pageable pageable, String userId);
//
//    PostResponse updatePost(String id, PostRequest request, String deletedMediaUrls, String userId);
//
//    void deletePost(String id, String userId);
//
//    PostResponse toggleLike(String postId, String userId);
//
//    Page<PostResponse> getPostsForCurrentUser(Pageable pageable, String userId);
//
//    PostResponse getPostById(String postId, String userId);
//}


package com.example.victorymoments.service;

import com.example.victorymoments.dto.PostRequest;
import com.example.victorymoments.dto.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface PostService {
    PostResponse createPost(PostRequest request);

    Page<PostResponse> getAllPosts(Pageable pageable, String userId);

    PostResponse updatePost(String id, PostRequest request, String deletedMediaUrls, String userId);

    void deletePost(String id, String userId);

    PostResponse toggleLike(String postId, String userId);

    Page<PostResponse> getPostsForCurrentUser(Pageable pageable, String userId);

    PostResponse getPostById(String postId, String userId);

    // THÊM 2 DÒNG NÀY VÀO INTERFACE
    Page<PostResponse> getPostsByEmail(String userEmail, Pageable pageable, String currentUserId);

    Page<PostResponse> getPostsByUserId(String targetUserId, Pageable pageable, String currentUserId);
}
