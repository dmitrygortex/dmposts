package com.example.contentcrm.dataaccess.repository;

import com.example.contentcrm.business.model.enums.Platform;
import com.example.contentcrm.business.model.enums.PublicationVariantStatus;
import com.example.contentcrm.dataaccess.entity.PublicationVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PublicationVariantRepository extends JpaRepository<PublicationVariantEntity, Long>, JpaSpecificationExecutor<PublicationVariantEntity> {
    List<PublicationVariantEntity> findByContentUnitIdOrderByPlatformAsc(Long contentUnitId);

    List<PublicationVariantEntity> findByStatusAndScheduledAtLessThanEqual(PublicationVariantStatus status, LocalDateTime scheduledAt);

    boolean existsByContentUnitIdAndPlatform(Long contentUnitId, Platform platform);

    Optional<PublicationVariantEntity> findByContentUnitIdAndPlatform(Long contentUnitId, Platform platform);

    long countByContentUnitId(Long contentUnitId);

    long countByStatus(PublicationVariantStatus status);
}
