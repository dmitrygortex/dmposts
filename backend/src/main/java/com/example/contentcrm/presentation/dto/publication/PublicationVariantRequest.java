package com.example.contentcrm.presentation.dto.publication;

import com.example.contentcrm.business.model.enums.Platform;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PublicationVariantRequest(
        @NotNull Long contentUnitId,
        @NotNull Platform platform,
        String adaptedText,
        LocalDateTime scheduledAt
) {
}
