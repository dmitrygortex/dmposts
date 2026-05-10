package com.example.contentcrm.presentation.dto.media;

import java.time.LocalDateTime;

public record MediaFileResponse(
        Long id,
        Long contentUnitId,
        Long taskId,
        String originalName,
        String mimeType,
        long size,
        String downloadUrl,
        Long uploadedById,
        LocalDateTime uploadedAt
) {
}
