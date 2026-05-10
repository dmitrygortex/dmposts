package com.example.contentcrm.dataaccess.integration;

import com.example.contentcrm.business.model.enums.Platform;
import org.springframework.stereotype.Component;

@Component
public class TelegramPublisher implements PlatformPublisher {
    @Override
    public Platform platform() {
        return Platform.TELEGRAM;
    }

    @Override
    public PublishResult publish(PublishRequest request) {
        return PublishResult.success(
                "telegram-" + request.variantId(),
                "https://t.me/mock_channel/" + request.variantId(),
                "{\"mock\":\"telegram-success\"}"
        );
    }
}
