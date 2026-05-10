package com.example.contentcrm.security;

import com.example.contentcrm.business.exception.ResourceNotFoundException;
import com.example.contentcrm.dataaccess.entity.UserEntity;
import com.example.contentcrm.dataaccess.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserProvider {
    private final UserRepository userRepository;

    public CurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<Long> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            return Optional.empty();
        }
        return Optional.of(securityUser.id());
    }

    public Long requireCurrentUserId() {
        return currentUserId().orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    public UserEntity requireCurrentUser() {
        return userRepository.findById(requireCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }
}
