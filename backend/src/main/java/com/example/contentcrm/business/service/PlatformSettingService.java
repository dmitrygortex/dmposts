package com.example.contentcrm.business.service;

import com.example.contentcrm.business.model.enums.Platform;
import com.example.contentcrm.presentation.dto.platform.PlatformSettingResponse;
import com.example.contentcrm.presentation.dto.platform.PlatformSettingUpdateRequest;

import java.util.List;

public interface PlatformSettingService {
    List<PlatformSettingResponse> list();

    PlatformSettingResponse update(Platform platform, PlatformSettingUpdateRequest request);

    PlatformSettingResponse test(Platform platform);
}
