package com.example.contentcrm.presentation.dto.publication;

import com.example.contentcrm.business.model.enums.PublicationAttemptStatus;

import java.time.LocalDateTime;

public record PublicationAttemptResponse(
        Long id,
        Long publicationVariantId,
        int attemptNumber,
        PublicationAttemptStatus status,
        String errorMessage,
        String responsePayload,
        LocalDateTime createdAt
) {
}
