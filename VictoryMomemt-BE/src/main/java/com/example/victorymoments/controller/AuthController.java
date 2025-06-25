package com.example.victorymoments.controller;

import com.example.victorymoments.request.AuthRequest;
import com.example.victorymoments.request.RegisterRequest;
import com.example.victorymoments.response.AuthResponse;
import com.example.victorymoments.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"https://shark-calm-externally.ngrok-free.app", "http://localhost:4200"})
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "Authentication and Registration endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @Operation(summary = "Login an existing user")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Verify a user's email with token")
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        boolean isVerified = authService.verifyEmail(token);
        Map<String, Object> body = new HashMap<>();
        body.put("success", isVerified);
        body.put("message", isVerified ? "Email verified successfully" : "Verification failed");
        return ResponseEntity.status(isVerified ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(body);
    }
}
