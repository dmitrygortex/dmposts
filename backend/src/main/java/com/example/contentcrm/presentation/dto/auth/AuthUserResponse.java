package com.example.contentcrm.presentation.dto.auth;

import com.example.contentcrm.business.model.enums.Role;

public record AuthUserResponse(
        Long id,
        String email,
        String fullName,
        Role role,
        boolean isActive
) {
}
