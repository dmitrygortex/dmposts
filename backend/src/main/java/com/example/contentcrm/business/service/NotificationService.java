package com.example.contentcrm.business.service;

import com.example.contentcrm.business.model.enums.NotificationType;
import com.example.contentcrm.dataaccess.entity.UserEntity;
import com.example.contentcrm.presentation.dto.common.PageResponse;
import com.example.contentcrm.presentation.dto.notification.NotificationResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void create(UserEntity user, NotificationType type, String message, String link);

    PageResponse<NotificationResponse> list(Long userId, boolean unreadOnly, Pageable pageable);

    NotificationResponse markRead(Long id, Long userId);

    void markAllRead(Long userId);

    long unreadCount(Long userId);
}
