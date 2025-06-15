// src/main/java/com/example/victorymoments/config/JwtTokenProvider.java
package com.example.victorymoments.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateToken(String email, String userId) { // Thêm userId
        SecretKey secretKey = getSigningKey();
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId) // Thêm userId vào claims
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        SecretKey secretKey = getSigningKey();
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public String getUserIdFromToken(String token) {
        SecretKey secretKey = getSigningKey();
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", String.class); // Trích xuất userId
    }

    @Deprecated
    public String getUsernameFromToken(String token) {
        return getEmailFromToken(token);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = getEmailFromToken(token);
            return username.equals(userDetails.getUsername());
        } catch (ExpiredJwtException e) {
            logger.warn("Token expired: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Invalid token: {}", e.getMessage());
        }
        return false;
    }
}