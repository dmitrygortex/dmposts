package com.example.contentcrm.presentation.dto.platform;

import com.example.contentcrm.business.model.enums.PlatformMode;
import jakarta.validation.constraints.NotNull;

public record PlatformSettingUpdateRequest(
        @NotNull Boolean enabled,
        @NotNull PlatformMode mode,
        String accessToken,
        String communityId,
        String apiVersion,
        String manualUrl,
        String instanceUrl
) {
    public PlatformSettingUpdateRequest(Boolean enabled, PlatformMode mode, String accessToken, String communityId, String apiVersion) {
        this(enabled, mode, accessToken, communityId, apiVersion, null, null);
    }

    public PlatformSettingUpdateRequest(Boolean enabled, PlatformMode mode, String accessToken, String communityId, String apiVersion, String manualUrl) {
        this(enabled, mode, accessToken, communityId, apiVersion, manualUrl, null);
    }
}
