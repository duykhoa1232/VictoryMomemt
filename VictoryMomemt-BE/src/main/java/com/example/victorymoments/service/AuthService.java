package com.example.victorymoments.service;

import com.example.victorymoments.request.AuthRequest;
import com.example.victorymoments.request.RegisterRequest;
import com.example.victorymoments.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(AuthRequest request);

    boolean verifyEmail(String token);
}
