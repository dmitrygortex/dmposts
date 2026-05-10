package com.example.contentcrm.business.service.impl;

import com.example.contentcrm.business.exception.BusinessRuleViolationException;
import com.example.contentcrm.business.exception.ResourceNotFoundException;
import com.example.contentcrm.business.model.enums.ApprovalStatus;
import com.example.contentcrm.business.model.enums.ContentUnitStatus;
import com.example.contentcrm.business.model.enums.NotificationType;
import com.example.contentcrm.business.service.ApprovalService;
import com.example.contentcrm.business.service.NotificationService;
import com.example.contentcrm.dataaccess.entity.ApprovalEntity;
import com.example.contentcrm.dataaccess.entity.ContentUnitEntity;
import com.example.contentcrm.dataaccess.entity.UserEntity;
import com.example.contentcrm.dataaccess.repository.ApprovalRepository;
import com.example.contentcrm.dataaccess.repository.ContentUnitRepository;
import com.example.contentcrm.dataaccess.repository.UserRepository;
import com.example.contentcrm.presentation.dto.approval.ApprovalDecisionRequest;
import com.example.contentcrm.presentation.dto.approval.ApprovalResponse;
import com.example.contentcrm.presentation.dto.approval.ApprovalSubmitRequest;
import com.example.contentcrm.presentation.mapper.ApprovalMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApprovalServiceImpl implements ApprovalService {
    private final ApprovalRepository approvalRepository;
    private final ContentUnitRepository contentUnitRepository;
    private final UserRepository userRepository;
    private final ApprovalMapper approvalMapper;
    private final NotificationService notificationService;

    public ApprovalServiceImpl(
            ApprovalRepository approvalRepository,
            ContentUnitRepository contentUnitRepository,
            UserRepository userRepository,
            ApprovalMapper approvalMapper,
            NotificationService notificationService
    ) {
        this.approvalRepository = approvalRepository;
        this.contentUnitRepository = contentUnitRepository;
        this.userRepository = userRepository;
        this.approvalMapper = approvalMapper;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponse> list(Long contentUnitId) {
        List<ApprovalEntity> approvals = contentUnitId == null
                ? approvalRepository.findAll()
                : approvalRepository.findByContentUnitIdOrderByCreatedAtDesc(contentUnitId);
        return approvals.stream().map(approvalMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ApprovalResponse submit(ApprovalSubmitRequest request) {
        ContentUnitEntity content = contentUnitRepository.findById(request.contentUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Content unit not found"));
        if (!StringUtils.hasText(content.getBaseText())) {
            throw new BusinessRuleViolationException("Content baseText is required before approval");
        }
        if (approvalRepository.existsByContentUnitIdAndStatus(content.getId(), ApprovalStatus.PENDING)) {
            throw new BusinessRuleViolationException("Content unit already has pending approval");
        }
        UserEntity reviewer = userRepository.findById(request.reviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));
        ApprovalEntity approval = new ApprovalEntity();
        approval.setContentUnit(content);
        approval.setReviewer(reviewer);
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setComment(request.comment());
        content.setStatus(ContentUnitStatus.ON_REVIEW);
        notificationService.create(reviewer, NotificationType.CONTENT_ON_REVIEW, "Материал на согласовании: " + content.getTitle(), "/approvals");
        return approvalMapper.toResponse(approvalRepository.save(approval));
    }

    @Override
    @Transactional
    public ApprovalResponse approve(Long id, ApprovalDecisionRequest request) {
        ApprovalEntity approval = findApproval(id);
        requirePending(approval);
        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setComment(request.comment());
        approval.setReviewedAt(LocalDateTime.now());
        approval.getContentUnit().setStatus(ContentUnitStatus.APPROVED);
        return approvalMapper.toResponse(approval);
    }

    @Override
    @Transactional
    public ApprovalResponse reject(Long id, ApprovalDecisionRequest request) {
        if (request == null || !StringUtils.hasText(request.comment())) {
            throw new BusinessRuleViolationException("Reject comment is required");
        }
        ApprovalEntity approval = findApproval(id);
        requirePending(approval);
        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setComment(request.comment());
        approval.setReviewedAt(LocalDateTime.now());
        approval.getContentUnit().setStatus(ContentUnitStatus.NEEDS_CHANGES);
        UserEntity responsible = approval.getContentUnit().getResponsibleUser();
        notificationService.create(responsible, NotificationType.CONTENT_REJECTED, "Материал отклонён: " + approval.getContentUnit().getTitle(), "/content-units/" + approval.getContentUnit().getId());
        return approvalMapper.toResponse(approval);
    }

    private ApprovalEntity findApproval(Long id) {
        return approvalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Approval not found"));
    }

    private void requirePending(ApprovalEntity approval) {
        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessRuleViolationException("Only PENDING approval can be reviewed");
        }
    }
}
