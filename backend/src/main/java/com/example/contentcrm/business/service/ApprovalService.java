package com.example.contentcrm.business.service;

import com.example.contentcrm.presentation.dto.approval.ApprovalDecisionRequest;
import com.example.contentcrm.presentation.dto.approval.ApprovalResponse;
import com.example.contentcrm.presentation.dto.approval.ApprovalSubmitRequest;

import java.util.List;

public interface ApprovalService {
    List<ApprovalResponse> list(Long contentUnitId);

    ApprovalResponse submit(ApprovalSubmitRequest request);

    ApprovalResponse approve(Long id, ApprovalDecisionRequest request);

    ApprovalResponse reject(Long id, ApprovalDecisionRequest request);
}
