package com.example.contentcrm.business.service.impl;

import com.example.contentcrm.business.exception.BusinessRuleViolationException;
import com.example.contentcrm.business.exception.ResourceNotFoundException;
import com.example.contentcrm.business.model.enums.ContentType;
import com.example.contentcrm.business.model.enums.ContentUnitStatus;
import com.example.contentcrm.business.service.ContentUnitService;
import com.example.contentcrm.business.workflow.ContentUnitStatusWorkflow;
import com.example.contentcrm.dataaccess.entity.ContentUnitEntity;
import com.example.contentcrm.dataaccess.entity.UserEntity;
import com.example.contentcrm.dataaccess.repository.ContentUnitRepository;
import com.example.contentcrm.dataaccess.repository.PublicationVariantRepository;
import com.example.contentcrm.dataaccess.repository.TaskRepository;
import com.example.contentcrm.dataaccess.repository.UserRepository;
import com.example.contentcrm.presentation.dto.content.ContentUnitRequest;
import com.example.contentcrm.presentation.dto.content.ContentUnitResponse;
import com.example.contentcrm.presentation.dto.content.ContentUnitStatusRequest;
import com.example.contentcrm.presentation.mapper.ContentUnitMapper;
import com.example.contentcrm.security.CurrentUserProvider;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContentUnitServiceImpl implements ContentUnitService {
    private final ContentUnitRepository contentUnitRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PublicationVariantRepository publicationVariantRepository;
    private final ContentUnitMapper mapper;
    private final CurrentUserProvider currentUserProvider;
    private final ContentUnitStatusWorkflow workflow;

    public ContentUnitServiceImpl(
            ContentUnitRepository contentUnitRepository,
            UserRepository userRepository,
            TaskRepository taskRepository,
            PublicationVariantRepository publicationVariantRepository,
            ContentUnitMapper mapper,
            CurrentUserProvider currentUserProvider,
            ContentUnitStatusWorkflow workflow
    ) {
        this.contentUnitRepository = contentUnitRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.publicationVariantRepository = publicationVariantRepository;
        this.mapper = mapper;
        this.currentUserProvider = currentUserProvider;
        this.workflow = workflow;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContentUnitResponse> list(ContentUnitStatus status, Long responsibleUserId, ContentType contentType, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return contentUnitRepository.findAll(spec(status, responsibleUserId, contentType, from, to), pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public ContentUnitResponse create(ContentUnitRequest request) {
        validatePublishDate(request.plannedPublishAt());
        ContentUnitEntity entity = new ContentUnitEntity();
        applyRequest(entity, request);
        UserEntity createdBy = currentUserProvider.currentUserId()
                .flatMap(userRepository::findById)
                .orElseGet(() -> request.responsibleUserId() == null ? firstUser() : findUser(request.responsibleUserId()));
        entity.setCreatedBy(createdBy);
        if (entity.getResponsibleUser() == null) {
            entity.setResponsibleUser(createdBy);
        }
        entity.setStatus(ContentUnitStatus.DRAFT);
        return toResponse(contentUnitRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ContentUnitResponse get(Long id) {
        return toResponse(findContent(id));
    }

    @Override
    @Transactional
    public ContentUnitResponse update(Long id, ContentUnitRequest request) {
        ContentUnitEntity entity = findContent(id);
        if (entity.getStatus() == ContentUnitStatus.PUBLISHED) {
            throw new BusinessRuleViolationException("Published content cannot be edited");
        }
        validatePublishDate(request.plannedPublishAt());
        applyRequest(entity, request);
        return toResponse(entity);
    }

    @Override
    @Transactional
    public ContentUnitResponse changeStatus(Long id, ContentUnitStatusRequest request) {
        ContentUnitEntity entity = findContent(id);
        if (!workflow.canDirectTransition(entity.getStatus(), request.status())) {
            throw new BusinessRuleViolationException("Unsupported direct content status transition");
        }
        entity.setStatus(request.status());
        return toResponse(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ContentUnitEntity entity = findContent(id);
        if (entity.getStatus() != ContentUnitStatus.DRAFT || publicationVariantRepository.countByContentUnitId(id) > 0) {
            throw new BusinessRuleViolationException("Only DRAFT content without publication variants can be deleted");
        }
        contentUnitRepository.delete(entity);
    }

    private void applyRequest(ContentUnitEntity entity, ContentUnitRequest request) {
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setBaseText(request.baseText());
        entity.setContentType(request.contentType());
        entity.setPlannedPublishAt(request.plannedPublishAt());
        entity.setResponsibleUser(request.responsibleUserId() == null ? null : findActiveUser(request.responsibleUserId()));
    }

    private void validatePublishDate(LocalDateTime plannedPublishAt) {
        if (plannedPublishAt != null && plannedPublishAt.isBefore(LocalDateTime.now())) {
            throw new BusinessRuleViolationException("plannedPublishAt cannot be in the past");
        }
    }

    private UserEntity findActiveUser(Long id) {
        UserEntity user = findUser(id);
        if (!user.isActive()) {
            throw new BusinessRuleViolationException("Inactive user cannot be responsible");
        }
        return user;
    }

    private UserEntity findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserEntity firstUser() {
        return userRepository.findAll().stream().findFirst().orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ContentUnitEntity findContent(Long id) {
        return contentUnitRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Content unit not found"));
    }

    private ContentUnitResponse toResponse(ContentUnitEntity entity) {
        return mapper.toResponse(
                entity,
                taskRepository.countByContentUnitId(entity.getId()),
                publicationVariantRepository.countByContentUnitId(entity.getId())
        );
    }

    private Specification<ContentUnitEntity> spec(ContentUnitStatus status, Long responsibleUserId, ContentType contentType, LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (responsibleUserId != null) predicates.add(cb.equal(root.get("responsibleUser").get("id"), responsibleUserId));
            if (contentType != null) predicates.add(cb.equal(root.get("contentType"), contentType));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("plannedPublishAt"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("plannedPublishAt"), to));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
