package com.example.contentcrm.presentation.dto.platform;

import com.example.contentcrm.business.model.enums.Platform;
import com.example.contentcrm.business.model.enums.PlatformMode;

import java.time.LocalDateTime;

public record PlatformSettingResponse(
        Platform platform,
        boolean enabled,
        PlatformMode mode,
        boolean tokenConfigured,
        String communityId,
        boolean communityConfigured,
        String manualUrl,
        String instanceUrl,
        String apiVersion,
        LocalDateTime updatedAt
) {
}
