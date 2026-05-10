package com.example.contentcrm.scheduler;

import com.example.contentcrm.business.service.PublicationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PublicationScheduler {
    private final PublicationService publicationService;

    public PublicationScheduler(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.publication-fixed-delay-ms}")
    public void publishDue() {
        publicationService.publishScheduledDuePublications();
    }
}
