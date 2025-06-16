package com.example.victorymoments.service;

import com.example.victorymoments.dto.AuthRequest;
import com.example.victorymoments.dto.AuthResponse;
import com.example.victorymoments.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request, HttpServletRequest requestContext, HttpServletResponse response);
    AuthResponse login(AuthRequest request);
    boolean verifyEmail(String token);
}