package com.example.shopflow_user.controller;

import com.example.shopflow_user.dto.*;
import com.example.shopflow_user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;

    @PostMapping("/auth/register")
    public Mono<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    public Mono<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
    @GetMapping("/profile")
    public Mono<Map<String, Object>> profile(Authentication authentication) {
        return Mono.just(Map.of(
                "message", "You are authenticated",
                "user", authentication.getName(),
                "authorities", authentication.getAuthorities()
        ));
    }
    @GetMapping("/{id}")
    public Mono<UserResponse> getUserById(@PathVariable String id) {
        return authService.getUserById(id);
    }

    @GetMapping("/admin/dashboard")
    public Mono<Map<String, Object>> admin(Authentication authentication) {
        return Mono.just(Map.of(
                "message", "Admin route accessed",
                "user", authentication.getName(),
                "authorities", authentication.getAuthorities()
        ));
    }

    @PostMapping("/auth/logout")
    public Mono<Void> logout(ServerWebExchange exchange) {

        return authService.logout(exchange);
    }



}



