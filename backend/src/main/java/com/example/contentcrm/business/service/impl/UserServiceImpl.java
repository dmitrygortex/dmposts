package com.example.contentcrm.business.service.impl;

import com.example.contentcrm.business.exception.BusinessRuleViolationException;
import com.example.contentcrm.business.exception.ResourceNotFoundException;
import com.example.contentcrm.business.model.enums.Role;
import com.example.contentcrm.business.service.UserService;
import com.example.contentcrm.dataaccess.entity.UserEntity;
import com.example.contentcrm.dataaccess.repository.UserRepository;
import com.example.contentcrm.presentation.dto.user.CreateUserRequest;
import com.example.contentcrm.presentation.dto.user.RoleUpdateRequest;
import com.example.contentcrm.presentation.dto.user.UpdateUserRequest;
import com.example.contentcrm.presentation.dto.user.UserResponse;
import com.example.contentcrm.presentation.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return userMapper.toResponse(findUser(id));
    }

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessRuleViolationException("Email already exists");
        }
        UserEntity user = new UserEntity();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(request.role());
        user.setActive(true);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        UserEntity user = findUser(id);
        user.setFullName(request.fullName());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateRole(Long id, RoleUpdateRequest request) {
        UserEntity user = findUser(id);
        if (user.getRole() == Role.OWNER && request.role() != Role.OWNER && userRepository.countByRoleAndActiveTrue(Role.OWNER) <= 1) {
            throw new BusinessRuleViolationException("Cannot remove role from the last active OWNER");
        }
        user.setRole(request.role());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse deactivate(Long id) {
        UserEntity user = findUser(id);
        if (user.getRole() == Role.OWNER && user.isActive() && userRepository.countByRoleAndActiveTrue(Role.OWNER) <= 1) {
            throw new BusinessRuleViolationException("Cannot deactivate the last active OWNER");
        }
        user.setActive(false);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse activate(Long id) {
        UserEntity user = findUser(id);
        user.setActive(true);
        return userMapper.toResponse(user);
    }

    private UserEntity findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
