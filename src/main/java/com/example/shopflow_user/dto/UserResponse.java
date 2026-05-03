package com.example.shopflow_user.dto;

import com.example.shopflow_user.model.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        Role role,
        Instant createdAt
) {

}
