package com.example.shopflow_user.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        Long expiresInSeconds
) {
}
