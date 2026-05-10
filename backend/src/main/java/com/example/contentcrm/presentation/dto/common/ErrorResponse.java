package com.example.contentcrm.presentation.dto.common;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorItem> details
) {
    public record FieldErrorItem(String field, String message) {
    }
}
