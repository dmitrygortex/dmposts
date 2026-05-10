package com.example.contentcrm.dataaccess.repository;

import com.example.contentcrm.dataaccess.entity.ContentUnitEntity;
import com.example.contentcrm.business.model.enums.ContentUnitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ContentUnitRepository extends JpaRepository<ContentUnitEntity, Long>, JpaSpecificationExecutor<ContentUnitEntity> {
    long countByStatus(ContentUnitStatus status);
}
