package com.example.contentcrm.presentation.mapper;

import com.example.contentcrm.dataaccess.entity.ContentUnitEntity;
import com.example.contentcrm.presentation.dto.content.ContentUnitResponse;
import org.springframework.stereotype.Component;

@Component
public class ContentUnitMapper {
    private final UserMapper userMapper;

    public ContentUnitMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public ContentUnitResponse toResponse(ContentUnitEntity entity, long tasksCount, long variantsCount) {
        return new ContentUnitResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getBaseText(),
                entity.getContentType(),
                entity.getStatus(),
                userMapper.toResponse(entity.getCreatedBy()),
                userMapper.toResponse(entity.getResponsibleUser()),
                entity.getPlannedPublishAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                tasksCount,
                variantsCount
        );
    }
}
