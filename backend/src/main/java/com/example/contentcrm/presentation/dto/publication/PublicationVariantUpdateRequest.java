package com.example.contentcrm.presentation.dto.publication;

import java.time.LocalDateTime;

public record PublicationVariantUpdateRequest(
        String adaptedText,
        LocalDateTime scheduledAt,
        String externalPostUrl
) {
}
