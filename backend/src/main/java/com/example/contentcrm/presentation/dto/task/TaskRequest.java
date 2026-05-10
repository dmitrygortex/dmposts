package com.example.contentcrm.presentation.dto.task;

import com.example.contentcrm.business.model.enums.TaskPriority;
import com.example.contentcrm.business.model.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskRequest(
        @NotNull Long contentUnitId,
        @NotBlank String title,
        String description,
        @NotNull TaskType type,
        @NotNull TaskPriority priority,
        @NotNull Long assigneeId,
        LocalDateTime deadline
) {
}
