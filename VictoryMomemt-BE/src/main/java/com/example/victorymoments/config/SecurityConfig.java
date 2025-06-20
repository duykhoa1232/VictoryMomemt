package com.example.victorymoments.config;

import com.example.victorymoments.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/swagger-resources/**"
                        ).permitAll()

                        // Auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/verify").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/auth/**").permitAll()

                        // Public post endpoints
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/{postId}/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/by-user-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/by-user/{userId}").permitAll()

                        // Public media files
                        .requestMatchers("/media/**").permitAll()

                        // FRIENDSHIP API
                        .requestMatchers("/api/friends/**").authenticated()

                        // Authenticated post + comment
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/posts/{id}/like").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/posts/{postId}/comments").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/{postId}/comments/{commentId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/{postId}/comments/{commentId}").authenticated()

                        // User profile
                        .requestMatchers("/api/users/profile/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/search").authenticated()

                        // Catch-all
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
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "https://afcc-113-22-96-116.ngrok-free.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
