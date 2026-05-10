package com.example.contentcrm.presentation.dto.task;

import com.example.contentcrm.business.model.enums.TaskPriority;
import com.example.contentcrm.business.model.enums.TaskType;

import java.time.LocalDateTime;

public record TaskUpdateRequest(
        String title,
        String description,
        TaskType type,
        TaskPriority priority,
        Long assigneeId,
        LocalDateTime deadline
) {
}
