package com.example.contentcrm.presentation.dto.user;

import com.example.contentcrm.business.model.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        Role role,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
