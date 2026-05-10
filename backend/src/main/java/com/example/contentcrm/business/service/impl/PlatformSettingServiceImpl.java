package com.example.contentcrm.business.service.impl;

import com.example.contentcrm.business.model.enums.Platform;
import com.example.contentcrm.business.model.enums.PlatformMode;
import com.example.contentcrm.business.exception.BusinessRuleViolationException;
import com.example.contentcrm.business.service.PlatformSettingService;
import com.example.contentcrm.dataaccess.entity.PlatformSettingEntity;
import com.example.contentcrm.dataaccess.repository.PlatformSettingRepository;
import com.example.contentcrm.presentation.dto.platform.PlatformSettingResponse;
import com.example.contentcrm.presentation.dto.platform.PlatformSettingUpdateRequest;
import com.example.contentcrm.presentation.mapper.PlatformSettingMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;

@Service
public class PlatformSettingServiceImpl implements PlatformSettingService {
    private final PlatformSettingRepository platformSettingRepository;
    private final PlatformSettingMapper mapper;

    public PlatformSettingServiceImpl(PlatformSettingRepository platformSettingRepository, PlatformSettingMapper mapper) {
        this.platformSettingRepository = platformSettingRepository;
        this.mapper = mapper;
    }

    @PostConstruct
    @Transactional
    public void seedDefaults() {
        seed(Platform.TELEGRAM, PlatformMode.AUTO);
        seed(Platform.VK, PlatformMode.AUTO_WITH_MANUAL_FALLBACK);
        seed(Platform.TENCHAT, PlatformMode.MANUAL);
        seed(Platform.SETKA, PlatformMode.MANUAL);
        seed(Platform.MAX, PlatformMode.MANUAL);
        seed(Platform.MASTODON, PlatformMode.AUTO_WITH_MANUAL_FALLBACK);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlatformSettingResponse> list() {
        return platformSettingRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public PlatformSettingResponse update(Platform platform, PlatformSettingUpdateRequest request) {
        PlatformSettingEntity setting = getOrCreate(platform);
        setting.setEnabled(request.enabled());
        setting.setMode(request.mode());
        if (StringUtils.hasText(request.accessToken())) {
            setting.setAccessTokenEncrypted(request.accessToken().trim());
        }
        if (request.communityId() != null) {
            setting.setCommunityId(StringUtils.hasText(request.communityId()) ? request.communityId().trim() : null);
        }
        if (request.manualUrl() != null) {
            setting.setManualUrl(StringUtils.hasText(request.manualUrl()) ? request.manualUrl().trim() : null);
        }
        if (platform == Platform.MASTODON && request.instanceUrl() != null) {
            setting.setInstanceUrl(normalizeMastodonInstanceUrl(request.instanceUrl()));
        }
        if (request.apiVersion() != null) {
            setting.setApiVersion(StringUtils.hasText(request.apiVersion()) ? request.apiVersion().trim() : "5.199");
        }
        return mapper.toResponse(setting);
    }

    @Override
    @Transactional(readOnly = true)
    public PlatformSettingResponse test(Platform platform) {
        return mapper.toResponse(getOrCreate(platform));
    }

    PlatformSettingEntity getOrCreate(Platform platform) {
        return platformSettingRepository.findByPlatform(platform).orElseGet(() -> {
            PlatformSettingEntity setting = new PlatformSettingEntity();
            setting.setPlatform(platform);
            setting.setEnabled(true);
            setting.setMode(defaultMode(platform));
            setting.setApiVersion("5.199");
            return platformSettingRepository.save(setting);
        });
    }

    private void seed(Platform platform, PlatformMode mode) {
        platformSettingRepository.findByPlatform(platform).orElseGet(() -> {
            PlatformSettingEntity setting = new PlatformSettingEntity();
            setting.setPlatform(platform);
            setting.setEnabled(true);
            setting.setMode(mode);
            setting.setApiVersion("5.199");
            return platformSettingRepository.save(setting);
        });
    }

    private PlatformMode defaultMode(Platform platform) {
        return switch (platform) {
            case TELEGRAM -> PlatformMode.AUTO;
            case VK -> PlatformMode.AUTO_WITH_MANUAL_FALLBACK;
            case MASTODON -> PlatformMode.AUTO_WITH_MANUAL_FALLBACK;
            case TENCHAT, SETKA, MAX, OTHER -> PlatformMode.MANUAL;
        };
    }

    private String normalizeMastodonInstanceUrl(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return null;
        }
        String value = rawValue.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleViolationException("Mastodon instance URL must be a valid https URL");
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || !StringUtils.hasText(uri.getHost())) {
            throw new BusinessRuleViolationException("Mastodon instance URL must be a valid https URL");
        }
        if (StringUtils.hasText(uri.getRawQuery()) || StringUtils.hasText(uri.getRawFragment())) {
            throw new BusinessRuleViolationException("Mastodon instance URL must not include query or fragment");
        }
        if (StringUtils.hasText(uri.getRawPath()) && !"/".equals(uri.getRawPath())) {
            throw new BusinessRuleViolationException("Mastodon instance URL must not include a path");
        }
        int port = uri.getPort();
        return port > 0 ? uri.getScheme() + "://" + uri.getHost() + ":" + port : uri.getScheme() + "://" + uri.getHost();
    }
}
