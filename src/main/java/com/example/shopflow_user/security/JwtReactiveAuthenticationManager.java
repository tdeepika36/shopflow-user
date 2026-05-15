package com.example.shopflow_user.security;

import com.example.shopflow_user.repository.UserRepository;
import com.example.shopflow_user.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public @NonNull Mono<Authentication> authenticate(@NonNull Authentication authentication) {

        Object credentials = authentication.getCredentials();

        if (credentials == null) {
            return Mono.error(new BadCredentialsException("Missing JWT token"));
        }

        String token = authentication.getCredentials().toString();

        if (token.isBlank()) {
            return Mono.error(new BadCredentialsException("Empty JWT token"));
        }
        if (!jwtService.isTokenValid(token)) {
            return Mono.error(new BadCredentialsException("Invalid JWT"));
        }
        String email = jwtService.extractEmail(token);
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new BadCredentialsException("user not found")))
                .map(user -> new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        token,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                ));
    }

}
