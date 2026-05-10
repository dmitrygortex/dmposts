package com.example.contentcrm.presentation.dto.approval;

import com.example.contentcrm.business.model.enums.ApprovalStatus;
import com.example.contentcrm.presentation.dto.content.ContentUnitResponse;
import com.example.contentcrm.presentation.dto.user.UserResponse;

import java.time.LocalDateTime;

public record ApprovalResponse(
        Long id,
        ContentUnitResponse contentUnit,
        UserResponse reviewer,
        ApprovalStatus status,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt
) {
}
