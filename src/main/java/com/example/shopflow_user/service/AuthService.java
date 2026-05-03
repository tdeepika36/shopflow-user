package com.example.shopflow_user.service;

import com.example.shopflow_user.dto.*;
import com.example.shopflow_user.model.Role;
import com.example.shopflow_user.model.User;
import com.example.shopflow_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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


}
