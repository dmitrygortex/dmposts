package com.example.contentcrm.business.service;

import com.example.contentcrm.business.model.enums.TaskPriority;
import com.example.contentcrm.business.model.enums.TaskStatus;
import com.example.contentcrm.presentation.dto.task.TaskRequest;
import com.example.contentcrm.presentation.dto.task.TaskResponse;
import com.example.contentcrm.presentation.dto.task.TaskStatusRequest;
import com.example.contentcrm.presentation.dto.task.TaskUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    Page<TaskResponse> list(Long currentUserId, boolean executorOnly, Long contentUnitId, TaskStatus status, TaskPriority priority, Boolean overdue, Pageable pageable);

    TaskResponse create(TaskRequest request);

    TaskResponse get(Long id);

    TaskResponse getForCurrentUser(Long id, Long currentUserId);

    TaskResponse update(Long id, TaskUpdateRequest request);

    TaskResponse changeStatus(Long id, TaskStatusRequest request, Long currentUserId);
}
