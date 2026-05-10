package com.example.contentcrm.presentation.controller;

import com.example.contentcrm.business.model.enums.Platform;
import com.example.contentcrm.business.model.enums.PublicationVariantStatus;
import com.example.contentcrm.business.service.PublicationService;
import com.example.contentcrm.presentation.dto.common.PageResponse;
import com.example.contentcrm.presentation.dto.publication.*;
import com.example.contentcrm.security.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/publication-variants")
@PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER')")
public class PublicationVariantController {
    private final PublicationService publicationService;
    private final CurrentUserProvider currentUserProvider;

    public PublicationVariantController(PublicationService publicationService, CurrentUserProvider currentUserProvider) {
        this.publicationService = publicationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public PageResponse<PublicationVariantResponse> list(
            @RequestParam(required = false) Long contentUnitId,
            @RequestParam(required = false) Platform platform,
            @RequestParam(required = false) PublicationVariantStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return PageResponse.from(publicationService.list(contentUnitId, platform, status, from, to, PageRequest.of(page, size)));
    }

    @PostMapping
    public PublicationVariantResponse create(@Valid @RequestBody PublicationVariantRequest request) {
        return publicationService.create(request);
    }

    @PostMapping("/bulk")
    public List<PublicationVariantResponse> bulkCreate(@Valid @RequestBody BulkPublicationVariantRequest request) {
        return publicationService.bulkCreate(request);
    }

    @GetMapping("/{id}")
    public PublicationVariantResponse get(@PathVariable Long id) {
        return publicationService.get(id);
    }

    @PatchMapping("/{id}")
    public PublicationVariantResponse update(@PathVariable Long id, @RequestBody PublicationVariantUpdateRequest request) {
        return publicationService.update(id, request);
    }

    @PostMapping("/{id}/schedule")
    public PublicationVariantResponse schedule(@PathVariable Long id, @Valid @RequestBody SchedulePublicationRequest request) {
        return publicationService.schedule(id, request);
    }

    @PostMapping("/{id}/publish-now")
    public PublicationVariantResponse publishNow(@PathVariable Long id) {
        return publicationService.publishNow(id);
    }

    @PostMapping("/{id}/retry")
    public PublicationVariantResponse retry(@PathVariable Long id) {
        return publicationService.retry(id);
    }

    @PostMapping("/{id}/switch-to-manual")
    public PublicationVariantResponse switchToManual(@PathVariable Long id, @RequestBody(required = false) SwitchToManualRequest request) {
        return publicationService.switchToManual(id, request == null ? new SwitchToManualRequest(null) : request);
    }

    @GetMapping("/{id}/manual")
    public ManualPublicationResponse manualDetails(@PathVariable Long id) {
        return publicationService.manualDetails(id);
    }

    @PostMapping("/{id}/manual-complete")
    public PublicationVariantResponse manualComplete(@PathVariable Long id, @Valid @RequestBody ManualCompleteRequest request) {
        return publicationService.manualComplete(id, request, currentUserProvider.requireCurrentUserId());
    }

    @PostMapping("/{id}/cancel")
    public PublicationVariantResponse cancel(@PathVariable Long id) {
        return publicationService.cancel(id);
    }
}
