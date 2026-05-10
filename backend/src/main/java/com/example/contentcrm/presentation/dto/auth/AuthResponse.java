package com.example.contentcrm.presentation.dto.auth;

public record AuthResponse(String accessToken, AuthUserResponse user) {
}
