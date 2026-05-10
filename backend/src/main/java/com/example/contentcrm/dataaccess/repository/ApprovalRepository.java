package com.example.contentcrm.dataaccess.repository;

import com.example.contentcrm.business.model.enums.ApprovalStatus;
import com.example.contentcrm.dataaccess.entity.ApprovalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRepository extends JpaRepository<ApprovalEntity, Long> {
    List<ApprovalEntity> findByContentUnitIdOrderByCreatedAtDesc(Long contentUnitId);

    List<ApprovalEntity> findByStatusOrderByCreatedAtDesc(ApprovalStatus status);

    boolean existsByContentUnitIdAndStatus(Long contentUnitId, ApprovalStatus status);
}
