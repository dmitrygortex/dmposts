package com.example.contentcrm.presentation.dto.content;

import com.example.contentcrm.business.model.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ContentUnitRequest(
        @NotBlank String title,
        String description,
        String baseText,
        @NotNull ContentType contentType,
        Long responsibleUserId,
        LocalDateTime plannedPublishAt
) {
}
