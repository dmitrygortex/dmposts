package com.example.contentcrm.dataaccess.repository;

import com.example.contentcrm.dataaccess.entity.MediaFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaFileRepository extends JpaRepository<MediaFileEntity, Long> {
    List<MediaFileEntity> findByContentUnitIdOrderByUploadedAtDesc(Long contentUnitId);

    List<MediaFileEntity> findByTaskIdOrderByUploadedAtDesc(Long taskId);
}
