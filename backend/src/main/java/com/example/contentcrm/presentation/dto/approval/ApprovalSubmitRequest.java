package com.example.contentcrm.presentation.dto.approval;

import jakarta.validation.constraints.NotNull;

public record ApprovalSubmitRequest(
        @NotNull Long contentUnitId,
        @NotNull Long reviewerId,
        String comment
) {
}
