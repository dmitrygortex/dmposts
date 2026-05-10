package com.example.contentcrm.presentation.controller;

import com.example.contentcrm.business.service.MediaService;
import com.example.contentcrm.presentation.dto.media.MediaFileResponse;
import com.example.contentcrm.security.CurrentUserProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    private final MediaService mediaService;
    private final CurrentUserProvider currentUserProvider;

    public MediaController(MediaService mediaService, CurrentUserProvider currentUserProvider) {
        this.mediaService = mediaService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER')")
    public List<MediaFileResponse> list(@RequestParam(required = false) Long contentUnitId, @RequestParam(required = false) Long taskId) {
        return mediaService.list(contentUnitId, taskId);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaFileResponse upload(
            @RequestParam MultipartFile file,
            @RequestParam Long contentUnitId,
            @RequestParam(required = false) Long taskId
    ) {
        return mediaService.upload(file, contentUnitId, taskId, currentUserProvider.requireCurrentUserId());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> download(@PathVariable Long id) {
        MediaService.DownloadedFile file = mediaService.download(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
                .body(file.resource());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER')")
    public void delete(@PathVariable Long id) {
        mediaService.delete(id);
    }
}
