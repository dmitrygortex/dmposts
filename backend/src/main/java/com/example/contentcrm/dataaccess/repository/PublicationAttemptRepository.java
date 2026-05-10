package com.example.contentcrm.dataaccess.repository;

import com.example.contentcrm.dataaccess.entity.PublicationAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicationAttemptRepository extends JpaRepository<PublicationAttemptEntity, Long> {
    int countByPublicationVariantId(Long publicationVariantId);

    List<PublicationAttemptEntity> findByPublicationVariantIdOrderByCreatedAtAsc(Long publicationVariantId);

    List<PublicationAttemptEntity> findByPublicationVariantContentUnitIdOrderByCreatedAtAsc(Long contentUnitId);
}
