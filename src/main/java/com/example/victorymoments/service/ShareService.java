package com.example.victorymoments.service;

import com.example.victorymoments.dto.ShareRequest;
import com.example.victorymoments.dto.ShareResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShareService {
    ShareResponse sharePost(ShareRequest request, String userEmail);
    void unsharePost(String shareId, String userEmail);
    Page<ShareResponse> getSharesByUserId(String userId, Pageable pageable);
    ShareResponse getShareById(String shareId);
}
