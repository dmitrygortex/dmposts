package com.example.contentcrm.presentation.controller;

import com.example.contentcrm.business.model.enums.Role;
import com.example.contentcrm.business.model.enums.TaskPriority;
import com.example.contentcrm.business.model.enums.TaskStatus;
import com.example.contentcrm.business.service.TaskService;
import com.example.contentcrm.presentation.dto.common.PageResponse;
import com.example.contentcrm.presentation.dto.task.TaskRequest;
import com.example.contentcrm.presentation.dto.task.TaskResponse;
import com.example.contentcrm.presentation.dto.task.TaskStatusRequest;
import com.example.contentcrm.presentation.dto.task.TaskUpdateRequest;
import com.example.contentcrm.security.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public PageResponse<TaskResponse> list(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Long contentUnitId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false, defaultValue = "false") boolean mine,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        boolean executorOnly = mine || user.role() == Role.EXECUTOR;
        return PageResponse.from(taskService.list(user.id(), executorOnly, contentUnitId, status, priority, overdue, PageRequest.of(page, size)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER')")
    public TaskResponse create(@Valid @RequestBody TaskRequest request) {
        return taskService.create(request);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable Long id, @AuthenticationPrincipal SecurityUser user) {
        return user.role() == Role.EXECUTOR ? taskService.getForCurrentUser(id, user.id()) : taskService.get(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER')")
    public TaskResponse update(@PathVariable Long id, @RequestBody TaskUpdateRequest request) {
        return taskService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse changeStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusRequest request, @AuthenticationPrincipal SecurityUser user) {
        return taskService.changeStatus(id, request, user.id());
    }
}
