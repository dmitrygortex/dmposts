package com.example.contentcrm.business.service;

import com.example.contentcrm.presentation.dto.auth.AuthResponse;
import com.example.contentcrm.presentation.dto.auth.LoginRequest;
import com.example.contentcrm.presentation.dto.auth.RegisterRequest;
import com.example.contentcrm.presentation.dto.auth.SetupStatusResponse;
import com.example.contentcrm.presentation.dto.user.UserResponse;

public interface AuthService {
    SetupStatusResponse setupStatus();

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse me(Long userId);
}
