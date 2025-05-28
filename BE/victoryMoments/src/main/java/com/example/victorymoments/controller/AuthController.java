package com.example.victorymoments.controller;

import com.example.victorymoments.dto.AuthRequest;
import com.example.victorymoments.dto.AuthResponse;
import com.example.victorymoments.dto.RegisterRequest;
import com.example.victorymoments.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            result.getFieldErrors().forEach(error ->
                    errorMessages.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
            );
            return ResponseEntity.badRequest().body(errorMessages.toString());
        }

        try {
            authService.register(request);
            return ResponseEntity.ok("User registered successfully");
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}