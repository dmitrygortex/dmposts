package com.example.contentcrm.presentation.dto.publication;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SchedulePublicationRequest(@NotNull LocalDateTime scheduledAt) {
}
