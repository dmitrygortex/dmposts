package com.example.contentcrm.business.service;

import com.example.contentcrm.presentation.dto.analytics.AnalyticsSummaryResponse;

public interface AnalyticsService {
    AnalyticsSummaryResponse summary(Long currentUserId);
}
