package com.example.contentcrm.business.service;

import com.example.contentcrm.presentation.dto.user.CreateUserRequest;
import com.example.contentcrm.presentation.dto.user.RoleUpdateRequest;
import com.example.contentcrm.presentation.dto.user.UpdateUserRequest;
import com.example.contentcrm.presentation.dto.user.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> list();

    UserResponse get(Long id);

    UserResponse create(CreateUserRequest request);

    UserResponse update(Long id, UpdateUserRequest request);

    UserResponse updateRole(Long id, RoleUpdateRequest request);

    UserResponse deactivate(Long id);

    UserResponse activate(Long id);
}
