package com.example.contentcrm.business.service;

import com.example.contentcrm.business.model.enums.ContentType;
import com.example.contentcrm.business.model.enums.ContentUnitStatus;
import com.example.contentcrm.presentation.dto.content.ContentUnitRequest;
import com.example.contentcrm.presentation.dto.content.ContentUnitResponse;
import com.example.contentcrm.presentation.dto.content.ContentUnitStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ContentUnitService {
    Page<ContentUnitResponse> list(ContentUnitStatus status, Long responsibleUserId, ContentType contentType, LocalDateTime from, LocalDateTime to, Pageable pageable);

    ContentUnitResponse create(ContentUnitRequest request);

    ContentUnitResponse get(Long id);

    ContentUnitResponse update(Long id, ContentUnitRequest request);

    ContentUnitResponse changeStatus(Long id, ContentUnitStatusRequest request);

    void delete(Long id);
}
