package com.example.contentcrm.presentation.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(@NotBlank String fullName) {
}
