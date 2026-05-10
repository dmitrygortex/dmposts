package com.example.contentcrm.dataaccess.integration;

import com.example.contentcrm.business.model.enums.Platform;

import java.util.List;

public record PublishRequest(
        Long variantId,
        Platform platform,
        String text,
        List<String> mediaPaths,
        String accessToken,
        String communityId,
        String apiVersion,
        String instanceUrl
) {
}
