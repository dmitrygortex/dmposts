package com.example.contentcrm.presentation.controller;

import com.example.contentcrm.business.service.AuthService;
import com.example.contentcrm.presentation.dto.auth.*;
import com.example.contentcrm.presentation.dto.user.UserResponse;
import com.example.contentcrm.security.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final CurrentUserProvider currentUserProvider;

    public AuthController(AuthService authService, CurrentUserProvider currentUserProvider) {
        this.authService = authService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/setup-status")
    public SetupStatusResponse setupStatus() {
        return authService.setupStatus();
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return authService.me(currentUserProvider.requireCurrentUserId());
    }
}
