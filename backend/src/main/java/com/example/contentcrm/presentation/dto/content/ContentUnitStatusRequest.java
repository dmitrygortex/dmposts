package com.example.contentcrm.presentation.dto.content;

import com.example.contentcrm.business.model.enums.ContentUnitStatus;
import jakarta.validation.constraints.NotNull;

public record ContentUnitStatusRequest(@NotNull ContentUnitStatus status) {
}
