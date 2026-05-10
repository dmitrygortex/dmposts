package com.example.contentcrm.presentation.mapper;

import com.example.contentcrm.business.model.enums.TaskStatus;
import com.example.contentcrm.dataaccess.entity.TaskEntity;
import com.example.contentcrm.presentation.dto.task.TaskResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TaskMapper {
    private final UserMapper userMapper;

    public TaskMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public TaskResponse toResponse(TaskEntity entity) {
        boolean overdue = entity.getDeadline() != null
                && entity.getDeadline().isBefore(LocalDateTime.now())
                && entity.getStatus() != TaskStatus.DONE
                && entity.getStatus() != TaskStatus.CANCELED;
        return new TaskResponse(
                entity.getId(),
                entity.getContentUnit().getId(),
                entity.getContentUnit().getTitle(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getType(),
                entity.getStatus(),
                entity.getPriority(),
                userMapper.toResponse(entity.getAssignee()),
                userMapper.toResponse(entity.getCreatedBy()),
                entity.getDeadline(),
                entity.getResultComment(),
                entity.getReviewComment(),
                overdue,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
