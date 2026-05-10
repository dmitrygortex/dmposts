package com.example.contentcrm.dataaccess.repository;

import com.example.contentcrm.business.model.enums.Platform;
import com.example.contentcrm.dataaccess.entity.PlatformSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformSettingRepository extends JpaRepository<PlatformSettingEntity, Long> {
    Optional<PlatformSettingEntity> findByPlatform(Platform platform);
}
