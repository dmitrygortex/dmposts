package com.example.contentcrm.presentation.mapper;

import com.example.contentcrm.dataaccess.entity.ApprovalEntity;
import com.example.contentcrm.presentation.dto.approval.ApprovalResponse;
import org.springframework.stereotype.Component;

@Component
public class ApprovalMapper {
    private final ContentUnitMapper contentUnitMapper;
    private final UserMapper userMapper;

    public ApprovalMapper(ContentUnitMapper contentUnitMapper, UserMapper userMapper) {
        this.contentUnitMapper = contentUnitMapper;
        this.userMapper = userMapper;
    }

    public ApprovalResponse toResponse(ApprovalEntity entity) {
        return new ApprovalResponse(
                entity.getId(),
                contentUnitMapper.toResponse(entity.getContentUnit(), 0, 0),
                userMapper.toResponse(entity.getReviewer()),
                entity.getStatus(),
                entity.getComment(),
                entity.getCreatedAt(),
                entity.getReviewedAt()
        );
    }
}
