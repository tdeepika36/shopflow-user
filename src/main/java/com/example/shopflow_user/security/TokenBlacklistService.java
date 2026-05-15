package com.example.shopflow_user.security;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Instant> blacklist=new ConcurrentHashMap<>();

    public Mono<Void> blacklist(String token,Instant expiresAt){
        blacklist.put(token,expiresAt);
        return Mono.empty();
    }
    public Mono<Boolean> isBlacklisted(String token){
        Instant expiresAt=blacklist.get(token);
        if(expiresAt==null){
            return Mono.just(false);
        }
        if(expiresAt.isBefore(Instant.now())){
            blacklist.remove(token);
            return Mono.just(false);
        }
        return Mono.just(true);
    }
}
