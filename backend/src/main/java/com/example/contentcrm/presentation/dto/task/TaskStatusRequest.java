package com.example.contentcrm.presentation.dto.task;

import com.example.contentcrm.business.model.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusRequest(@NotNull TaskStatus status, String comment) {
}
