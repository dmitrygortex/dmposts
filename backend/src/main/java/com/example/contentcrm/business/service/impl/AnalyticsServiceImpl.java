package com.example.contentcrm.business.service.impl;

import com.example.contentcrm.business.model.enums.ContentUnitStatus;
import com.example.contentcrm.business.model.enums.PublicationVariantStatus;
import com.example.contentcrm.business.model.enums.TaskStatus;
import com.example.contentcrm.business.service.AnalyticsService;
import com.example.contentcrm.dataaccess.repository.ContentUnitRepository;
import com.example.contentcrm.dataaccess.repository.NotificationRepository;
import com.example.contentcrm.dataaccess.repository.PublicationVariantRepository;
import com.example.contentcrm.dataaccess.repository.TaskRepository;
import com.example.contentcrm.presentation.dto.analytics.AnalyticsSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {
    private final ContentUnitRepository contentUnitRepository;
    private final TaskRepository taskRepository;
    private final PublicationVariantRepository publicationVariantRepository;
    private final NotificationRepository notificationRepository;

    public AnalyticsServiceImpl(
            ContentUnitRepository contentUnitRepository,
            TaskRepository taskRepository,
            PublicationVariantRepository publicationVariantRepository,
            NotificationRepository notificationRepository
    ) {
        this.contentUnitRepository = contentUnitRepository;
        this.taskRepository = taskRepository;
        this.publicationVariantRepository = publicationVariantRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse summary(Long currentUserId) {
        Map<String, Long> content = Map.of(
                "inProgress", contentUnitRepository.countByStatus(ContentUnitStatus.IN_PROGRESS),
                "onReview", contentUnitRepository.countByStatus(ContentUnitStatus.ON_REVIEW),
                "scheduled", contentUnitRepository.countByStatus(ContentUnitStatus.SCHEDULED),
                "published", contentUnitRepository.countByStatus(ContentUnitStatus.PUBLISHED),
                "manualRequired", publicationVariantRepository.countByStatus(PublicationVariantStatus.MANUAL_REQUIRED)
        );
        Map<String, Long> tasks = Map.of(
                "todo", taskRepository.countByAssigneeIdAndStatus(currentUserId, TaskStatus.TODO),
                "inProgress", taskRepository.countByAssigneeIdAndStatus(currentUserId, TaskStatus.IN_PROGRESS),
                "onReview", taskRepository.countByAssigneeIdAndStatus(currentUserId, TaskStatus.ON_REVIEW),
                "overdue", taskRepository.countByDeadlineBeforeAndStatusNotIn(LocalDateTime.now(), List.of(TaskStatus.DONE, TaskStatus.CANCELED))
        );
        Map<String, Long> publications = Map.of(
                "scheduled", publicationVariantRepository.countByStatus(PublicationVariantStatus.SCHEDULED),
                "published", publicationVariantRepository.countByStatus(PublicationVariantStatus.PUBLISHED),
                "manualRequired", publicationVariantRepository.countByStatus(PublicationVariantStatus.MANUAL_REQUIRED),
                "manualCompleted", publicationVariantRepository.countByStatus(PublicationVariantStatus.MANUAL_COMPLETED)
        );
        Map<String, Long> notifications = Map.of(
                "unread", notificationRepository.countByUserIdAndReadFalse(currentUserId)
        );
        return new AnalyticsSummaryResponse(content, tasks, publications, notifications);
    }
}
