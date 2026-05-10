package com.example.contentcrm.presentation.controller;

import com.example.contentcrm.business.model.enums.ContentType;
import com.example.contentcrm.business.model.enums.ContentUnitStatus;
import com.example.contentcrm.business.service.ContentUnitService;
import com.example.contentcrm.presentation.dto.common.PageResponse;
import com.example.contentcrm.presentation.dto.content.ContentUnitBaseTextRequest;
import com.example.contentcrm.presentation.dto.content.ContentUnitRequest;
import com.example.contentcrm.presentation.dto.content.ContentUnitResponse;
import com.example.contentcrm.presentation.dto.content.ContentUnitStatusRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/content-units")
public class ContentUnitController {
    private final ContentUnitService contentUnitService;

    public ContentUnitController(ContentUnitService contentUnitService) {
        this.contentUnitService = contentUnitService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER')")
    public PageResponse<ContentUnitResponse> list(
            @RequestParam(required = false) ContentUnitStatus status,
            @RequestParam(required = false) Long responsibleUserId,
            @RequestParam(required = false) ContentType contentType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return PageResponse.from(contentUnitService.list(status, responsibleUserId, contentType, from, to, PageRequest.of(page, size)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER')")
    public ContentUnitResponse create(@Valid @RequestBody ContentUnitRequest request) {
        return contentUnitService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER','EXECUTOR')")
    public ContentUnitResponse get(@PathVariable Long id) {
        return contentUnitService.get(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER')")
    public ContentUnitResponse update(@PathVariable Long id, @Valid @RequestBody ContentUnitRequest request) {
        return contentUnitService.update(id, request);
    }

    @PatchMapping("/{id}/base-text")
    @PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER','EXECUTOR')")
    public ContentUnitResponse updateBaseText(@PathVariable Long id, @RequestBody ContentUnitBaseTextRequest request) {
        return contentUnitService.updateBaseText(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER')")
    public ContentUnitResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ContentUnitStatusRequest request) {
        return contentUnitService.changeStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER')")
    public void delete(@PathVariable Long id) {
        contentUnitService.delete(id);
    }
}
