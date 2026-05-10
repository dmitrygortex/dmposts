package com.example.contentcrm.presentation.dto.analytics;

import java.util.Map;

public record AnalyticsSummaryResponse(
        Map<String, Long> content,
        Map<String, Long> tasks,
        Map<String, Long> publications,
        Map<String, Long> notifications
) {
}
