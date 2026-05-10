package com.example.contentcrm.presentation.controller;

import com.example.contentcrm.business.service.NotificationService;
import com.example.contentcrm.presentation.dto.common.PageResponse;
import com.example.contentcrm.presentation.dto.notification.NotificationResponse;
import com.example.contentcrm.security.CurrentUserProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final CurrentUserProvider currentUserProvider;

    public NotificationController(NotificationService notificationService, CurrentUserProvider currentUserProvider) {
        this.notificationService = notificationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public PageResponse<NotificationResponse> list(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return notificationService.list(currentUserProvider.requireCurrentUserId(), unreadOnly, PageRequest.of(page, size));
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable Long id) {
        return notificationService.markRead(id, currentUserProvider.requireCurrentUserId());
    }

    @PatchMapping("/read-all")
    public void markAllRead() {
        notificationService.markAllRead(currentUserProvider.requireCurrentUserId());
    }
}
