package com.example.contentcrm.presentation.controller;

import com.example.contentcrm.business.service.ApprovalService;
import com.example.contentcrm.presentation.dto.approval.ApprovalDecisionRequest;
import com.example.contentcrm.presentation.dto.approval.ApprovalResponse;
import com.example.contentcrm.presentation.dto.approval.ApprovalSubmitRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {
    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER')")
    public List<ApprovalResponse> list(@RequestParam(required = false) Long contentUnitId) {
        return approvalService.list(contentUnitId);
    }

    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER')")
    public ApprovalResponse submit(@Valid @RequestBody ApprovalSubmitRequest request) {
        return approvalService.submit(request);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('OWNER')")
    public ApprovalResponse approve(@PathVariable Long id, @RequestBody(required = false) ApprovalDecisionRequest request) {
        return approvalService.approve(id, request == null ? new ApprovalDecisionRequest(null) : request);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('OWNER')")
    public ApprovalResponse reject(@PathVariable Long id, @RequestBody ApprovalDecisionRequest request) {
        return approvalService.reject(id, request);
    }
}
