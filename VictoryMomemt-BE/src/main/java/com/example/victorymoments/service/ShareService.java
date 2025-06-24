package com.example.victorymoments.service;

import com.example.victorymoments.request.ShareRequest;
import com.example.victorymoments.response.ShareResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShareService {
    ShareResponse sharePost(ShareRequest request, String userEmail);
    ShareResponse getShareById(String shareId);
    void unsharePost(String shareId, String userEmail);
    Page<ShareResponse> getSharesByUserId(String userId, Pageable pageable);
}
