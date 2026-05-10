package com.example.contentcrm.business.service.impl;

import com.example.contentcrm.business.exception.BusinessRuleViolationException;
import com.example.contentcrm.business.exception.ResourceNotFoundException;
import com.example.contentcrm.business.model.enums.Role;
import com.example.contentcrm.business.service.AuthService;
import com.example.contentcrm.dataaccess.entity.UserEntity;
import com.example.contentcrm.dataaccess.repository.UserRepository;
import com.example.contentcrm.presentation.dto.auth.AuthResponse;
import com.example.contentcrm.presentation.dto.auth.LoginRequest;
import com.example.contentcrm.presentation.dto.auth.RegisterRequest;
import com.example.contentcrm.presentation.dto.auth.SetupStatusResponse;
import com.example.contentcrm.presentation.dto.user.UserResponse;
import com.example.contentcrm.presentation.mapper.UserMapper;
import com.example.contentcrm.security.JwtService;
import com.example.contentcrm.security.SecurityUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public SetupStatusResponse setupStatus() {
        boolean hasUsers = userRepository.count() > 0;
        return new SetupStatusResponse(hasUsers, !hasUsers);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.count() > 0) {
            throw new BusinessRuleViolationException("Registration is closed after first OWNER registration");
        }
        UserEntity user = new UserEntity();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(Role.OWNER);
        user.setActive(true);
        UserEntity saved = userRepository.save(user);
        return response(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            throw new BusinessRuleViolationException("Invalid email or password");
        }
        UserEntity user = userRepository.findByEmailIgnoreCase(request.email())
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new BusinessRuleViolationException("User is deactivated or not found"));
        return response(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AuthResponse response(UserEntity user) {
        String token = jwtService.generate(new SecurityUser(user));
        return new AuthResponse(token, userMapper.toAuthResponse(user));
    }
}
