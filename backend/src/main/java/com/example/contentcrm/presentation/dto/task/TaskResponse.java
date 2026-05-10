package com.example.contentcrm.presentation.dto.task;

import com.example.contentcrm.business.model.enums.TaskPriority;
import com.example.contentcrm.business.model.enums.TaskStatus;
import com.example.contentcrm.business.model.enums.TaskType;
import com.example.contentcrm.presentation.dto.user.UserResponse;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        Long contentUnitId,
        String contentUnitTitle,
        String title,
        String description,
        TaskType type,
        TaskStatus status,
        TaskPriority priority,
        UserResponse assignee,
        UserResponse createdBy,
        LocalDateTime deadline,
        String resultComment,
        String reviewComment,
        boolean overdue,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
