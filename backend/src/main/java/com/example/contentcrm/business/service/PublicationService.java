package com.example.contentcrm.business.service;

import com.example.contentcrm.business.model.enums.Platform;
import com.example.contentcrm.business.model.enums.PublicationVariantStatus;
import com.example.contentcrm.presentation.dto.publication.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicationService {
    Page<PublicationVariantResponse> list(Long contentUnitId, Platform platform, PublicationVariantStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable);

    PublicationVariantResponse create(PublicationVariantRequest request);

    List<PublicationVariantResponse> bulkCreate(BulkPublicationVariantRequest request);

    PublicationVariantResponse get(Long id);

    PublicationVariantResponse update(Long id, PublicationVariantUpdateRequest request);

    PublicationVariantResponse schedule(Long id, SchedulePublicationRequest request);

    PublicationVariantResponse publishNow(Long id);

    PublicationVariantResponse retry(Long id);

    PublicationVariantResponse switchToManual(Long id, SwitchToManualRequest request);

    ManualPublicationResponse manualDetails(Long id);

    PublicationVariantResponse manualComplete(Long id, ManualCompleteRequest request, Long currentUserId);

    PublicationVariantResponse cancel(Long id);

    List<PublicationAttemptResponse> getAttemptsByVariant(Long variantId);

    List<PublicationAttemptResponse> getAttemptsByContentUnit(Long contentUnitId);

    void publishScheduledDuePublications();
}
