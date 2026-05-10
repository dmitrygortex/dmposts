package com.example.contentcrm.business.service.impl;

import com.example.contentcrm.business.exception.BusinessRuleViolationException;
import com.example.contentcrm.business.exception.ResourceNotFoundException;
import com.example.contentcrm.business.model.enums.NotificationType;
import com.example.contentcrm.business.model.enums.Role;
import com.example.contentcrm.business.model.enums.TaskPriority;
import com.example.contentcrm.business.model.enums.TaskStatus;
import com.example.contentcrm.business.service.NotificationService;
import com.example.contentcrm.business.service.TaskService;
import com.example.contentcrm.business.workflow.TaskStatusWorkflow;
import com.example.contentcrm.dataaccess.entity.ContentUnitEntity;
import com.example.contentcrm.dataaccess.entity.TaskEntity;
import com.example.contentcrm.dataaccess.entity.UserEntity;
import com.example.contentcrm.dataaccess.repository.ContentUnitRepository;
import com.example.contentcrm.dataaccess.repository.TaskRepository;
import com.example.contentcrm.dataaccess.repository.UserRepository;
import com.example.contentcrm.presentation.dto.task.TaskRequest;
import com.example.contentcrm.presentation.dto.task.TaskResponse;
import com.example.contentcrm.presentation.dto.task.TaskStatusRequest;
import com.example.contentcrm.presentation.dto.task.TaskUpdateRequest;
import com.example.contentcrm.presentation.mapper.TaskMapper;
import com.example.contentcrm.security.CurrentUserProvider;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final ContentUnitRepository contentUnitRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final CurrentUserProvider currentUserProvider;
    private final TaskStatusWorkflow workflow;
    private final NotificationService notificationService;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            ContentUnitRepository contentUnitRepository,
            UserRepository userRepository,
            TaskMapper taskMapper,
            CurrentUserProvider currentUserProvider,
            TaskStatusWorkflow workflow,
            NotificationService notificationService
    ) {
        this.taskRepository = taskRepository;
        this.contentUnitRepository = contentUnitRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
        this.currentUserProvider = currentUserProvider;
        this.workflow = workflow;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> list(Long currentUserId, boolean executorOnly, Long contentUnitId, TaskStatus status, TaskPriority priority, Boolean overdue, Pageable pageable) {
        return taskRepository.findAll(spec(currentUserId, executorOnly, contentUnitId, status, priority, overdue), pageable)
                .map(taskMapper::toResponse);
    }

    @Override
    @Transactional
    public TaskResponse create(TaskRequest request) {
        ContentUnitEntity content = contentUnitRepository.findById(request.contentUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Content unit not found"));
        UserEntity assignee = findActiveUser(request.assigneeId());
        TaskEntity task = new TaskEntity();
        task.setContentUnit(content);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setType(request.type());
        task.setPriority(request.priority());
        task.setAssignee(assignee);
        task.setCreatedBy(currentUserProvider.currentUserId().flatMap(userRepository::findById).orElse(assignee));
        task.setDeadline(request.deadline());
        task.setStatus(TaskStatus.TODO);
        TaskEntity saved = taskRepository.save(task);
        notificationService.create(assignee, NotificationType.TASK_ASSIGNED, "Назначена задача: " + task.getTitle(), "/tasks/" + saved.getId());
        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse get(Long id) {
        return taskMapper.toResponse(findTask(id));
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getForCurrentUser(Long id, Long currentUserId) {
        TaskEntity task = findTask(id);
        if (!task.getAssignee().getId().equals(currentUserId)) {
            throw new BusinessRuleViolationException("Executor can access only own tasks");
        }
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse update(Long id, TaskUpdateRequest request) {
        TaskEntity task = findTask(id);
        if (request.title() != null) task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());
        if (request.type() != null) task.setType(request.type());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.assigneeId() != null) task.setAssignee(findActiveUser(request.assigneeId()));
        if (request.deadline() != null) task.setDeadline(request.deadline());
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse changeStatus(Long id, TaskStatusRequest request, Long currentUserId) {
        TaskEntity task = findTask(id);
        UserEntity actor = currentUserId == null ? task.getAssignee() : userRepository.findById(currentUserId).orElse(task.getAssignee());
        boolean ownTask = task.getAssignee().getId().equals(actor.getId());
        if (!workflow.canTransition(task.getStatus(), request.status(), actor.getRole(), ownTask)) {
            throw new BusinessRuleViolationException("Unsupported task status transition");
        }
        if (actor.getRole() == Role.EXECUTOR && !ownTask) {
            throw new BusinessRuleViolationException("Executor can update only own tasks");
        }
        if (request.status() == TaskStatus.ON_REVIEW) {
            task.setResultComment(request.comment());
            notificationService.create(task.getCreatedBy(), NotificationType.TASK_ON_REVIEW, "Задача на проверке: " + task.getTitle(), "/tasks/" + task.getId());
        } else if (request.status() == TaskStatus.DONE || request.status() == TaskStatus.IN_PROGRESS) {
            task.setReviewComment(request.comment());
        }
        task.setStatus(request.status());
        return taskMapper.toResponse(task);
    }

    private TaskEntity findTask(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private UserEntity findActiveUser(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isActive()) {
            throw new BusinessRuleViolationException("Task cannot be assigned to inactive user");
        }
        return user;
    }

    private Specification<TaskEntity> spec(Long currentUserId, boolean executorOnly, Long contentUnitId, TaskStatus status, TaskPriority priority, Boolean overdue) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (executorOnly && currentUserId != null) predicates.add(cb.equal(root.get("assignee").get("id"), currentUserId));
            if (contentUnitId != null) predicates.add(cb.equal(root.get("contentUnit").get("id"), contentUnitId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (priority != null) predicates.add(cb.equal(root.get("priority"), priority));
            if (Boolean.TRUE.equals(overdue)) {
                predicates.add(cb.lessThan(root.get("deadline"), LocalDateTime.now()));
                predicates.add(root.get("status").in(List.of(TaskStatus.DONE, TaskStatus.CANCELED)).not());
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
