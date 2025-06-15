package com.example.victorymoments.service.impl;

import com.example.victorymoments.config.JwtTokenProvider;
import com.example.victorymoments.dto.AuthRequest;
import com.example.victorymoments.dto.AuthResponse;
import com.example.victorymoments.dto.RegisterRequest;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.service.AuthService;
import com.example.victorymoments.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthServiceImpl.class.getName());

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           AuthenticationManager authenticationManager,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    @Override
    public AuthResponse register(RegisterRequest request, HttpServletRequest requestContext, HttpServletResponse response) {
        validateRegisterRequest(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email is already in use");
        }

        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ValidationException("Phone number is already in use");
        }

        User user = buildNewUser(request);
        userRepository.save(user);

        try {
            emailService.sendWelcomeEmail(user);
        } catch (Exception e) {
            LOGGER.severe("Failed to send welcome email: " + e.getMessage());
            throw new ValidationException("Failed to send welcome email: " + e.getMessage());
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId()); // Truyền userId
        return new AuthResponse(token);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("User not found"));
            String token = jwtTokenProvider.generateToken(email, user.getId());
            return new AuthResponse(token);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Override
    public boolean verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ValidationException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return true;
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null || request.getName() == null) {
            throw new ValidationException("Missing required fields");
        }
    }

    private User buildNewUser(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRoles(Collections.singletonList("USER"));
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerified(false);
        return user;
    }
}