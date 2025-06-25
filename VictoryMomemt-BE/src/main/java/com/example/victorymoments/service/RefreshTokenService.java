package com.example.victorymoments.service;

import com.example.victorymoments.entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(String userId);

    String verifyRefreshToken(String token);

    void deleteByUserId(String userId);
}
