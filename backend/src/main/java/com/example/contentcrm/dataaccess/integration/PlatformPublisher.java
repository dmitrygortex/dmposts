package com.example.contentcrm.dataaccess.integration;

import com.example.contentcrm.business.model.enums.Platform;

public interface PlatformPublisher {
    Platform platform();

    PublishResult publish(PublishRequest request);
}
