package com.example.contentcrm.presentation.mapper;

import com.example.contentcrm.dataaccess.entity.NotificationEntity;
import com.example.contentcrm.presentation.dto.notification.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponse toResponse(NotificationEntity entity) {
        return new NotificationResponse(
                entity.getId(),
                entity.getType(),
                entity.getMessage(),
                entity.getLink(),
                entity.isRead(),
                entity.getCreatedAt()
        );
    }
}
