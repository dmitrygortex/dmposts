package com.example.contentcrm.business.service;

import com.example.contentcrm.presentation.dto.media.MediaFileResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaService {
    List<MediaFileResponse> list(Long contentUnitId, Long taskId);

    MediaFileResponse upload(MultipartFile file, Long contentUnitId, Long taskId, Long currentUserId);

    DownloadedFile download(Long id);

    void delete(Long id);

    record DownloadedFile(Resource resource, String filename, String mimeType) {
    }
}
