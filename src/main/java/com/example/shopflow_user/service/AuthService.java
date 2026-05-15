package com.example.shopflow_user.service;

import com.example.shopflow_user.dto.*;
import com.example.shopflow_user.model.Role;
import com.example.shopflow_user.model.User;
import com.example.shopflow_user.repository.UserRepository;
import com.example.shopflow_user.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public Mono<UserResponse> register(RegisterRequest request) {
        return userRepository.existsByEmail(request.email())
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(new RuntimeException("Email already exists"));
                    }
                    User user = User.builder()
                            .email(request.email())
                            .passwordHash(passwordEncoder.encode(request.password()))
                            .role(Role.CUSTOMER)
                            .createdAt(Instant.now())
                            .build();
                    return userRepository.save(user);
                })
                .map(savedUser -> new UserResponse(
                        savedUser.getId(),
                        savedUser.getEmail(),
                        savedUser.getRole(),
                        savedUser.getCreatedAt()
                ));
    }

    public Mono<TokenResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.email())
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid email or password")))
                .flatMap(user -> {
                    boolean passwordMatches = passwordEncoder.matches(request.password(),
                            user.getPasswordHash());
                    if (!passwordMatches) {
                        return Mono.error(new RuntimeException("Invalid email or password"));
                    }
                    String token = jwtService.generateToken(
                            user.getId(),
                            user.getEmail(),
                            user.getRole()
                    );
                    return Mono.just(new TokenResponse(
                            token,
                            "Bearer",
                            jwtService.getExpirationSeconds()
                    ));
                });
    }

    public Mono<Void> logout(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new BadCredentialsException("Missing Authorization header"));
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            return Mono.error(new BadCredentialsException("Invalid token"));
        }

        return tokenBlacklistService.blacklist(
                token,
                jwtService.extractExpiration(token)
        );
    }

    public Mono<UserResponse> getUserById(String id) {
        Long userId = Long.valueOf(id);
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCreatedAt()
                ));
    }


}
