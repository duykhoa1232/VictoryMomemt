package com.example.victorymoments.service;

import com.example.victorymoments.dto.ShareRequest;
import com.example.victorymoments.dto.ShareResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShareService {
    ShareResponse sharePost(ShareRequest request, String userEmail);
    ShareResponse getShareById(String shareId);
    void unsharePost(String shareId, String userEmail);
    Page<ShareResponse> getSharesByUserId(String userId, Pageable pageable);
}
