package com.example.contentcrm.presentation.mapper;

import com.example.contentcrm.dataaccess.entity.UserEntity;
import com.example.contentcrm.presentation.dto.auth.AuthUserResponse;
import com.example.contentcrm.presentation.dto.user.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(UserEntity user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public AuthUserResponse toAuthResponse(UserEntity user) {
        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isActive()
        );
    }
}
