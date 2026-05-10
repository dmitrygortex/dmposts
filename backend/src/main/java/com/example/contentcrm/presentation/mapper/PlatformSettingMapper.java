package com.example.contentcrm.presentation.mapper;

import com.example.contentcrm.dataaccess.entity.PlatformSettingEntity;
import com.example.contentcrm.presentation.dto.platform.PlatformSettingResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PlatformSettingMapper {
    public PlatformSettingResponse toResponse(PlatformSettingEntity entity) {
        return new PlatformSettingResponse(
                entity.getPlatform(),
                entity.isEnabled(),
                entity.getMode(),
                StringUtils.hasText(entity.getAccessTokenEncrypted()),
                entity.getCommunityId(),
                StringUtils.hasText(entity.getCommunityId()),
                entity.getManualUrl(),
                entity.getInstanceUrl(),
                entity.getApiVersion(),
                entity.getUpdatedAt()
        );
    }
}
