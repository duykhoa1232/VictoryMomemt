package com.example.victorymoments.config;

import com.example.victorymoments.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        logger.info("DEBUG (JwtFilter): Processing request for URL: {} Method: {}", request.getRequestURI(), request.getMethod()); // Dùng logger

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) { // Chỉ kiểm tra token có text, sau đó validate
                logger.info("DEBUG (JwtFilter): Found JWT token: {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "..."); // Log token (một phần)
                if (jwtTokenProvider.validateToken(jwt)) {
                    String username = jwtTokenProvider.getEmailFromToken(jwt); // Sửa thành getEmailFromToken nếu đó là subject
                    logger.info("DEBUG (JwtFilter): Token valid, username: {}", username); // Log username từ token

                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("DEBUG (JwtFilter): Authentication successful for user: {}", username);
                } else {
                    logger.warn("DEBUG (JwtFilter): JWT token is NOT valid for URL: {}", request.getRequestURI()); // Log không hợp lệ
                }
            } else {
                logger.info("DEBUG (JwtFilter): No JWT token found in Authorization header for URL: {}", request.getRequestURI()); // Log không tìm thấy token
            }
        } catch (Exception ex) {
            logger.error("DEBUG (JwtFilter): Could not set user authentication in security context for URL: {}", request.getRequestURI(), ex); // Log ngoại lệ
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}