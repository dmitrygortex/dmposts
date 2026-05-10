package com.example.contentcrm.presentation.mapper;

import com.example.contentcrm.dataaccess.entity.PublicationAttemptEntity;
import com.example.contentcrm.dataaccess.entity.PublicationVariantEntity;
import com.example.contentcrm.presentation.dto.publication.PublicationAttemptResponse;
import com.example.contentcrm.presentation.dto.publication.PublicationVariantResponse;
import org.springframework.stereotype.Component;

@Component
public class PublicationMapper {
    public PublicationVariantResponse toVariantResponse(PublicationVariantEntity entity) {
        Long manualUserId = entity.getManualCompletedBy() == null ? null : entity.getManualCompletedBy().getId();
        return new PublicationVariantResponse(
                entity.getId(),
                entity.getContentUnit().getId(),
                entity.getContentUnit().getTitle(),
                entity.getPlatform(),
                entity.getAdaptedText(),
                entity.getScheduledAt(),
                entity.getStatus(),
                entity.getExternalPostId(),
                entity.getExternalPostUrl(),
                entity.getErrorMessage(),
                entity.getManualInstruction(),
                manualUserId,
                entity.getManualCompletedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public PublicationAttemptResponse toAttemptResponse(PublicationAttemptEntity entity) {
        return new PublicationAttemptResponse(
                entity.getId(),
                entity.getPublicationVariant().getId(),
                entity.getAttemptNumber(),
                entity.getStatus(),
                entity.getErrorMessage(),
                entity.getResponsePayload(),
                entity.getCreatedAt()
        );
    }
}
