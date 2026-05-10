package com.example.contentcrm.presentation.mapper;

import com.example.contentcrm.dataaccess.entity.MediaFileEntity;
import com.example.contentcrm.presentation.dto.media.MediaFileResponse;
import org.springframework.stereotype.Component;

@Component
public class MediaFileMapper {
    public MediaFileResponse toResponse(MediaFileEntity entity) {
        Long taskId = entity.getTask() == null ? null : entity.getTask().getId();
        return new MediaFileResponse(
                entity.getId(),
                entity.getContentUnit().getId(),
                taskId,
                entity.getOriginalName(),
                entity.getMimeType(),
                entity.getSize(),
                "/api/media/" + entity.getId() + "/download",
                entity.getUploadedBy().getId(),
                entity.getUploadedAt()
        );
    }
}
