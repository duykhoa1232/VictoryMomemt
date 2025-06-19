package com.example.victorymoments.config;

import com.example.victorymoments.service.CustomUserDetailsService;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Swagger access
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/index.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/webjars/**",
                    "/swagger-resources/**"
                ).permitAll()
                // Auth APIs
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/verify").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/auth/**").permitAll()

                // ĐÃ SỬA: Đặt các endpoint công khai cụ thể của Post lên đầu để đảm bảo không bị chặn bởi các quy tắc chung
                // Các endpoint liên quan đến bài đăng và bình luận công khai (GET)
                .requestMatchers(HttpMethod.GET, "/api/posts/{postId}/comments").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts/by-user-email").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts/by-user/{userId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll() // Bao gồm single post GET và getAllPosts

                // Cho phép truy cập các file media
                .requestMatchers("/media/**").permitAll()

                // Authenticated routes (Yêu cầu xác thực)
                .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/posts/{id}/like").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/posts/{postId}/comments").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/posts/{postId}/comments/{commentId}").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/posts/{postId}/comments/{commentId}").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users/profile").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/profile").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/users/profile/avatar").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users/search").authenticated() // Thêm nếu search users cần auth

                // Bất kỳ yêu cầu nào khác đều yêu cầu xác thực
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
