package com.example.victorymoments.controller;

import com.example.victorymoments.dto.AuthRequest;
import com.example.victorymoments.dto.AuthResponse;
import com.example.victorymoments.dto.RegisterRequest;
import com.example.victorymoments.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Endpoints for user registration, login, and email verification")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request,
            HttpServletRequest requestContext,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request, requestContext, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean isVerified = authService.verifyEmail(token);
        return isVerified
                ? ResponseEntity.ok("Email verified successfully")
                : ResponseEntity.badRequest().body("Verification failed");
    }
}
