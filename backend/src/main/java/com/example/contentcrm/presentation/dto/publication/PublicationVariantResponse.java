package com.example.contentcrm.presentation.dto.publication;

import com.example.contentcrm.business.model.enums.Platform;
import com.example.contentcrm.business.model.enums.PublicationVariantStatus;

import java.time.LocalDateTime;

public record PublicationVariantResponse(
        Long id,
        Long contentUnitId,
        String contentUnitTitle,
        Platform platform,
        String adaptedText,
        LocalDateTime scheduledAt,
        PublicationVariantStatus status,
        String externalPostId,
        String externalPostUrl,
        String errorMessage,
        String manualInstruction,
        Long manualCompletedById,
        LocalDateTime manualCompletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
