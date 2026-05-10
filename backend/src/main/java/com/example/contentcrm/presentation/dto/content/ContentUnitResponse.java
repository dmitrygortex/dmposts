package com.example.contentcrm.presentation.dto.content;

import com.example.contentcrm.business.model.enums.ContentType;
import com.example.contentcrm.business.model.enums.ContentUnitStatus;
import com.example.contentcrm.presentation.dto.user.UserResponse;

import java.time.LocalDateTime;

public record ContentUnitResponse(
        Long id,
        String title,
        String description,
        String baseText,
        ContentType contentType,
        ContentUnitStatus status,
        UserResponse createdBy,
        UserResponse responsibleUser,
        LocalDateTime plannedPublishAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long tasksCount,
        long variantsCount
) {
}
