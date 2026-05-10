package com.example.contentcrm.presentation.controller;

import com.example.contentcrm.business.service.PublicationService;
import com.example.contentcrm.presentation.dto.publication.PublicationAttemptResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publication-attempts")
@PreAuthorize("hasAnyRole('OWNER','CONTENT_MANAGER')")
public class PublicationAttemptController {
    private final PublicationService publicationService;

    public PublicationAttemptController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @GetMapping(params = "variantId")
    public List<PublicationAttemptResponse> byVariant(@RequestParam Long variantId) {
        return publicationService.getAttemptsByVariant(variantId);
    }

    @GetMapping(params = "contentUnitId")
    public List<PublicationAttemptResponse> byContent(@RequestParam Long contentUnitId) {
        return publicationService.getAttemptsByContentUnit(contentUnitId);
    }
}
