package com.example.contentcrm.presentation.dto.notification;

import com.example.contentcrm.business.model.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String message,
        String link,
        boolean isRead,
        LocalDateTime createdAt
) {
}
