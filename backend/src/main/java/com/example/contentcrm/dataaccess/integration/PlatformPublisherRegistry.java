package com.example.contentcrm.dataaccess.integration;

import com.example.contentcrm.business.model.enums.Platform;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PlatformPublisherRegistry {
    private final Map<Platform, PlatformPublisher> publishers = new EnumMap<>(Platform.class);

    public PlatformPublisherRegistry(List<PlatformPublisher> publishers) {
        publishers.forEach(publisher -> this.publishers.putIfAbsent(publisher.platform(), publisher));
    }

    public Optional<PlatformPublisher> find(Platform platform) {
        return Optional.ofNullable(publishers.get(platform));
    }
}
