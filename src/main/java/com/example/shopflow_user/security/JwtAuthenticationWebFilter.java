package com.example.shopflow_user.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationWebFilter implements WebFilter {
    private final JwtReactiveAuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    private final ServerWebExchangeMatcher protectedRoutes =
            ServerWebExchangeMatchers.pathMatchers(
                    "/users/**",
                    "/admin/**",
                    "/auth/logout"
            );

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return protectedRoutes.matches(exchange)
                .flatMap(matchResult -> {
                    if (!matchResult.isMatch()) {
                        return chain.filter(exchange);
                    }
                    String token = extractToken(exchange);
                    if (token == null) {
                        return chain.filter(exchange);
                    }
                    Authentication authenticationToken =
                            new UsernamePasswordAuthenticationToken(null, token);

                    Mono<Authentication> validateUser =
                            authenticationManager.authenticate(authenticationToken)
                                    .doOnSubscribe(s -> log.info("JWT validation started"))
                                    .doOnNext(auth -> log.info("JWT validation completed for {}", auth.getName()));
                    Mono<Boolean> checkBlacklist =
                            tokenBlacklistService.isBlacklisted(token)
                                    .doOnSubscribe(s -> log.info("Blacklist check started"))
                                    .doOnSuccess(result -> log.info("Blacklist check completed : {}", result));

                    return Mono.zip(validateUser, checkBlacklist)
                            .flatMap(tuple -> {
                                Authentication authentication = tuple.getT1();
                                Boolean isBlacklisted = tuple.getT2();
                                if (isBlacklisted) {
                                    log.warn("Token rejected because it is blacklisted");
                                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                    return exchange.getResponse().setComplete();
                                }
                                SecurityContextImpl securityContext =
                                        new SecurityContextImpl(authentication);

                                return chain.filter(exchange)
                                        .contextWrite(
                                                ReactiveSecurityContextHolder.withSecurityContext(
                                                        Mono.just(securityContext)
                                                )
                                        );
                            });
                });

    }
    private String extractToken(ServerWebExchange exchange){
        String authHeader=exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);
        if(authHeader==null || !authHeader.startsWith("Bearer")){
            return null;
        }
        return authHeader.substring(7);
    }

}


