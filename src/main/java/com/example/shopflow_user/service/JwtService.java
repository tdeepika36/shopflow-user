package com.example.shopflow_user.service;

import com.example.shopflow_user.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;

    @Getter
    private final long expirationSeconds;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration.minutes}") long expirationMinutes
    ){
        this.key= Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds=expirationMinutes * 60;

    }

    public String generateToken(Long userId, String email, Role role) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .subject(email)
                .claim("userId",userId)
                .claim("role",role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();

    }
    public Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public boolean isTokenValid(String token){
        try{
            Claims claims=parseClaims(token);
            return claims.getExpiration()
                    .toInstant()
                    .isAfter(Instant.now());
        }
        catch(Exception ex){
            return false;
        }
    }
    public String extractEmail(String token){
        return parseClaims(token).getSubject();
    }
    public String extractRole(String token){
        return parseClaims(token).get("role",String.class);
    }
    public Long extractUserId(String token){
        return parseClaims(token).get("userId",Long.class);
    }

}
