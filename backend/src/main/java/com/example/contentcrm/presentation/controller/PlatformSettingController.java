package com.example.contentcrm.presentation.controller;

import com.example.contentcrm.business.model.enums.Platform;
import com.example.contentcrm.business.service.PlatformSettingService;
import com.example.contentcrm.presentation.dto.platform.PlatformSettingResponse;
import com.example.contentcrm.presentation.dto.platform.PlatformSettingUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/platform-settings")
@PreAuthorize("hasRole('OWNER')")
public class PlatformSettingController {
    private final PlatformSettingService platformSettingService;

    public PlatformSettingController(PlatformSettingService platformSettingService) {
        this.platformSettingService = platformSettingService;
    }

    @GetMapping
    public List<PlatformSettingResponse> list() {
        return platformSettingService.list();
    }

    @PatchMapping("/{platform}")
    public PlatformSettingResponse update(@PathVariable Platform platform, @Valid @RequestBody PlatformSettingUpdateRequest request) {
        return platformSettingService.update(platform, request);
    }

    @PostMapping("/{platform}/test")
    public PlatformSettingResponse test(@PathVariable Platform platform) {
        return platformSettingService.test(platform);
    }
}
