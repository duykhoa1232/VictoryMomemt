package com.example.victorymoments.service.impl;

import com.example.victorymoments.config.JwtTokenProvider;
import com.example.victorymoments.entity.User;
import com.example.victorymoments.exception.BadRequestException;
import com.example.victorymoments.exception.ErrorCode;
import com.example.victorymoments.exception.NotFoundException;
import com.example.victorymoments.exception.UnauthorizedException;
import com.example.victorymoments.repository.UserRepository;
import com.example.victorymoments.request.AuthRequest;
import com.example.victorymoments.request.RegisterRequest;
import com.example.victorymoments.response.AuthResponse;
import com.example.victorymoments.service.AuthService;
import com.example.victorymoments.service.EmailService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {
        validateRegistrationRequest(request);

        User user = buildNewUser(request);
        userRepository.save(user);

        emailService.sendWelcomeEmailAsync(user);

        String token = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getId().toString(),
                user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                user.getName()
        );

        return new AuthResponse(token);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, request.getEmail()));

            String token = jwtTokenProvider.generateToken(
                    user.getEmail(),
                    user.getId().toString(),
                    user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                    user.getName()
            );

            return new AuthResponse(token);
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID, e);
        }
    }

    @Transactional
    @Override
    public boolean verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BadRequestException(ErrorCode.VERIFY_TOKEN_INVALID));
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null || request.getName() == null) {
            throw new BadRequestException(ErrorCode.AUTH_MISSING_FIELDS);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(ErrorCode.EMAIL_EXISTS, request.getEmail());
        }
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException(ErrorCode.USER_PHONE_EXISTS, request.getPhoneNumber());
        }
    }

    private User buildNewUser(RegisterRequest request) {
        return User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .roles(Collections.singletonList("USER"))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .verificationToken(UUID.randomUUID().toString())
                .emailVerified(false)
                .build();
    }
}
