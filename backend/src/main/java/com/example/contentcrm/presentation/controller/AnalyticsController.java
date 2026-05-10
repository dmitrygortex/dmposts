package com.example.contentcrm.presentation.controller;

import com.example.contentcrm.business.service.AnalyticsService;
import com.example.contentcrm.presentation.dto.analytics.AnalyticsSummaryResponse;
import com.example.contentcrm.security.CurrentUserProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;
    private final CurrentUserProvider currentUserProvider;

    public AnalyticsController(AnalyticsService analyticsService, CurrentUserProvider currentUserProvider) {
        this.analyticsService = analyticsService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/summary")
    public AnalyticsSummaryResponse summary() {
        return analyticsService.summary(currentUserProvider.requireCurrentUserId());
    }
}
