package com.example.contentcrm.business.service.impl;

import com.example.contentcrm.business.exception.BusinessRuleViolationException;
import com.example.contentcrm.business.exception.ResourceNotFoundException;
import com.example.contentcrm.business.model.enums.*;
import com.example.contentcrm.business.service.PublicationService;
import com.example.contentcrm.dataaccess.entity.*;
import com.example.contentcrm.dataaccess.integration.PlatformPublisherRegistry;
import com.example.contentcrm.dataaccess.integration.PublishRequest;
import com.example.contentcrm.dataaccess.integration.PublishResult;
import com.example.contentcrm.dataaccess.repository.*;
import com.example.contentcrm.presentation.dto.publication.*;
import com.example.contentcrm.presentation.mapper.ContentUnitMapper;
import com.example.contentcrm.presentation.mapper.MediaFileMapper;
import com.example.contentcrm.presentation.mapper.PublicationMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class PublicationServiceImpl implements PublicationService {
    private static final Set<PublicationVariantStatus> FINAL_STATUSES = Set.of(
            PublicationVariantStatus.PUBLISHED,
            PublicationVariantStatus.MANUAL_COMPLETED
    );

    private final PublicationVariantRepository variantRepository;
    private final PublicationAttemptRepository attemptRepository;
    private final ContentUnitRepository contentUnitRepository;
    private final UserRepository userRepository;
    private final MediaFileRepository mediaFileRepository;
    private final PlatformSettingServiceImpl platformSettingService;
    private final NotificationServiceImpl notificationService;
    private final PublicationMapper publicationMapper;
    private final ContentUnitMapper contentUnitMapper;
    private final MediaFileMapper mediaFileMapper;
    private final PlatformPublisherRegistry publisherRegistry;
    private final ZoneId applicationZoneId;

    public PublicationServiceImpl(
            PublicationVariantRepository variantRepository,
            PublicationAttemptRepository attemptRepository,
            ContentUnitRepository contentUnitRepository,
            UserRepository userRepository,
            MediaFileRepository mediaFileRepository,
            PlatformSettingServiceImpl platformSettingService,
            NotificationServiceImpl notificationService,
            PublicationMapper publicationMapper,
            ContentUnitMapper contentUnitMapper,
            MediaFileMapper mediaFileMapper,
            PlatformPublisherRegistry publisherRegistry,
            @Value("${app.time-zone:Europe/Moscow}") String applicationTimeZone
    ) {
        this.variantRepository = variantRepository;
        this.attemptRepository = attemptRepository;
        this.contentUnitRepository = contentUnitRepository;
        this.userRepository = userRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.platformSettingService = platformSettingService;
        this.notificationService = notificationService;
        this.publicationMapper = publicationMapper;
        this.contentUnitMapper = contentUnitMapper;
        this.mediaFileMapper = mediaFileMapper;
        this.publisherRegistry = publisherRegistry;
        this.applicationZoneId = ZoneId.of(applicationTimeZone);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicationVariantResponse> list(Long contentUnitId, Platform platform, PublicationVariantStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return variantRepository.findAll(spec(contentUnitId, platform, status, from, to), pageable)
                .map(publicationMapper::toVariantResponse);
    }

    @Override
    @Transactional
    public PublicationVariantResponse create(PublicationVariantRequest request) {
        ContentUnitEntity content = contentUnitRepository.findById(request.contentUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Content unit not found"));
        if (content.getStatus() != ContentUnitStatus.APPROVED && content.getStatus() != ContentUnitStatus.SCHEDULED && content.getStatus() != ContentUnitStatus.PARTIALLY_PUBLISHED) {
            throw new BusinessRuleViolationException("Publication variants can be created only for APPROVED content");
        }
        PlatformSettingEntity setting = platformSettingService.getOrCreate(request.platform());
        if (!setting.isEnabled()) {
            throw new BusinessRuleViolationException("Platform is disabled");
        }
        if (variantRepository.existsByContentUnitIdAndPlatform(content.getId(), request.platform())) {
            throw new BusinessRuleViolationException("Publication variant already exists for platform");
        }
        PublicationVariantEntity entity = new PublicationVariantEntity();
        entity.setContentUnit(content);
        entity.setPlatform(request.platform());
        entity.setAdaptedText(request.adaptedText());
        entity.setScheduledAt(request.scheduledAt());
        entity.setManualInstruction(defaultManualInstruction(request.platform()));
        entity.setStatus(StringUtils.hasText(request.adaptedText()) ? PublicationVariantStatus.READY : PublicationVariantStatus.DRAFT);
        return publicationMapper.toVariantResponse(variantRepository.save(entity));
    }

    @Override
    @Transactional
    public List<PublicationVariantResponse> bulkCreate(BulkPublicationVariantRequest request) {
        return request.platforms().stream().map(platform -> {
            if (variantRepository.existsByContentUnitIdAndPlatform(request.contentUnitId(), platform)) {
                return publicationMapper.toVariantResponse(variantRepository.findByContentUnitIdAndPlatform(request.contentUnitId(), platform).orElseThrow());
            }
            ContentUnitEntity content = contentUnitRepository.findById(request.contentUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Content unit not found"));
            return create(new PublicationVariantRequest(content.getId(), platform, content.getBaseText(), content.getPlannedPublishAt()));
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PublicationVariantResponse get(Long id) {
        return publicationMapper.toVariantResponse(findVariant(id));
    }

    @Override
    @Transactional
    public PublicationVariantResponse update(Long id, PublicationVariantUpdateRequest request) {
        PublicationVariantEntity variant = findVariant(id);
        boolean finalStatus = FINAL_STATUSES.contains(variant.getStatus());
        boolean editsPublicationBody = request.adaptedText() != null || request.scheduledAt() != null;
        if (finalStatus && editsPublicationBody) {
            throw new BusinessRuleViolationException("Final publication variant cannot be edited");
        }
        if (request.adaptedText() != null) {
            variant.setAdaptedText(request.adaptedText());
            if (StringUtils.hasText(request.adaptedText()) && variant.getStatus() == PublicationVariantStatus.DRAFT) {
                variant.setStatus(PublicationVariantStatus.READY);
            }
        }
        if (request.scheduledAt() != null) {
            variant.setScheduledAt(request.scheduledAt());
        }
        if (request.externalPostUrl() != null) {
            if (finalStatus && !StringUtils.hasText(request.externalPostUrl())) {
                throw new BusinessRuleViolationException("externalPostUrl is required");
            }
            variant.setExternalPostUrl(request.externalPostUrl().trim());
        }
        return publicationMapper.toVariantResponse(variant);
    }

    @Override
    @Transactional
    public PublicationVariantResponse schedule(Long id, SchedulePublicationRequest request) {
        PublicationVariantEntity variant = findVariant(id);
        if (!StringUtils.hasText(variant.getAdaptedText())) {
            throw new BusinessRuleViolationException("adaptedText is required to schedule publication");
        }
        if (request.scheduledAt().isBefore(now())) {
            throw new BusinessRuleViolationException("scheduledAt cannot be in the past");
        }
        variant.setScheduledAt(request.scheduledAt());
        variant.setStatus(PublicationVariantStatus.SCHEDULED);
        recalculateContentStatus(variant.getContentUnit());
        return publicationMapper.toVariantResponse(variant);
    }

    @Override
    @Transactional
    public PublicationVariantResponse publishNow(Long id) {
        PublicationVariantEntity variant = findVariant(id);
        publishVariant(variant);
        recalculateContentStatus(variant.getContentUnit());
        return publicationMapper.toVariantResponse(variant);
    }

    @Override
    @Transactional
    public PublicationVariantResponse retry(Long id) {
        PublicationVariantEntity variant = findVariant(id);
        if (variant.getStatus() != PublicationVariantStatus.MANUAL_REQUIRED) {
            throw new BusinessRuleViolationException("Only MANUAL_REQUIRED variant can be retried");
        }
        publishVariant(variant);
        recalculateContentStatus(variant.getContentUnit());
        return publicationMapper.toVariantResponse(variant);
    }

    @Override
    @Transactional
    public PublicationVariantResponse switchToManual(Long id, SwitchToManualRequest request) {
        PublicationVariantEntity variant = findVariant(id);
        variant.setStatus(PublicationVariantStatus.MANUAL_REQUIRED);
        variant.setErrorMessage(request == null || !StringUtils.hasText(request.reason()) ? "Manual publication requested" : request.reason());
        variant.setManualInstruction(defaultManualInstruction(variant.getPlatform()));
        recalculateContentStatus(variant.getContentUnit());
        return publicationMapper.toVariantResponse(variant);
    }

    @Override
    @Transactional(readOnly = true)
    public ManualPublicationResponse manualDetails(Long id) {
        PublicationVariantEntity variant = findVariant(id);
        List<MediaFileEntity> mediaFiles = mediaFileRepository.findByContentUnitIdOrderByUploadedAtDesc(variant.getContentUnit().getId());
        return new ManualPublicationResponse(
                contentUnitMapper.toResponse(variant.getContentUnit(), 0, variantRepository.countByContentUnitId(variant.getContentUnit().getId())),
                publicationMapper.toVariantResponse(variant),
                platformUrl(variant),
                mediaFiles.stream().map(mediaFileMapper::toResponse).toList()
        );
    }

    @Override
    @Transactional
    public PublicationVariantResponse manualComplete(Long id, ManualCompleteRequest request, Long currentUserId) {
        PublicationVariantEntity variant = findVariant(id);
        if (variant.getStatus() != PublicationVariantStatus.MANUAL_REQUIRED) {
            throw new BusinessRuleViolationException("Manual complete is allowed only from MANUAL_REQUIRED");
        }
        if (!StringUtils.hasText(request.externalPostUrl())) {
            throw new BusinessRuleViolationException("externalPostUrl is required");
        }
        UserEntity user = userRepository.findById(currentUserId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        variant.setExternalPostUrl(request.externalPostUrl());
        variant.setStatus(PublicationVariantStatus.MANUAL_COMPLETED);
        variant.setManualCompletedBy(user);
        variant.setManualCompletedAt(LocalDateTime.now());
        variant.setErrorMessage(null);
        recalculateContentStatus(variant.getContentUnit());
        return publicationMapper.toVariantResponse(variant);
    }

    @Override
    @Transactional
    public PublicationVariantResponse cancel(Long id) {
        PublicationVariantEntity variant = findVariant(id);
        if (!Set.of(PublicationVariantStatus.DRAFT, PublicationVariantStatus.READY, PublicationVariantStatus.SCHEDULED).contains(variant.getStatus())) {
            throw new BusinessRuleViolationException("Only DRAFT, READY or SCHEDULED variant can be canceled");
        }
        variant.setStatus(PublicationVariantStatus.CANCELED);
        recalculateContentStatus(variant.getContentUnit());
        return publicationMapper.toVariantResponse(variant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicationAttemptResponse> getAttemptsByVariant(Long variantId) {
        return attemptRepository.findByPublicationVariantIdOrderByCreatedAtAsc(variantId)
                .stream().map(publicationMapper::toAttemptResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicationAttemptResponse> getAttemptsByContentUnit(Long contentUnitId) {
        return attemptRepository.findByPublicationVariantContentUnitIdOrderByCreatedAtAsc(contentUnitId)
                .stream().map(publicationMapper::toAttemptResponse).toList();
    }

    @Override
    @Transactional
    public void publishScheduledDuePublications() {
        List<PublicationVariantEntity> due = variantRepository.findByStatusAndScheduledAtLessThanEqual(PublicationVariantStatus.SCHEDULED, now());
        due.forEach(variant -> {
            publishVariant(variant);
            recalculateContentStatus(variant.getContentUnit());
        });
    }

    private void publishVariant(PublicationVariantEntity variant) {
        if (FINAL_STATUSES.contains(variant.getStatus())) {
            return;
        }
        if (!StringUtils.hasText(variant.getAdaptedText())) {
            throw new BusinessRuleViolationException("adaptedText is required to publish");
        }
        PlatformSettingEntity setting = platformSettingService.getOrCreate(variant.getPlatform());
        if (!setting.isEnabled()) {
            markManualRequired(variant, "Platform is disabled");
            return;
        }
        if (setting.getMode() == PlatformMode.MANUAL) {
            markManualRequired(variant, "Platform works in manual mode");
            return;
        }
        variant.setStatus(PublicationVariantStatus.PUBLISHING);
        PublishResult result = publisherRegistry.find(variant.getPlatform())
                .map(publisher -> publisher.publish(new PublishRequest(
                        variant.getId(),
                        variant.getPlatform(),
                        variant.getAdaptedText(),
                        mediaFileRepository.findByContentUnitIdOrderByUploadedAtDesc(variant.getContentUnit().getId()).stream()
                                .filter(media -> media.getMimeType() != null && media.getMimeType().startsWith("image/"))
                                .map(MediaFileEntity::getPath)
                                .toList(),
                        setting.getAccessTokenEncrypted(),
                        setting.getCommunityId(),
                        setting.getApiVersion(),
                        setting.getInstanceUrl()
                )))
                .orElseGet(() -> PublishResult.failure("No publisher configured for platform", "{\"error\":\"no-publisher\"}"));
        createAttempt(variant, result);
        if (result.success()) {
            variant.setStatus(PublicationVariantStatus.PUBLISHED);
            variant.setExternalPostId(result.externalPostId());
            variant.setExternalPostUrl(result.externalPostUrl());
            variant.setErrorMessage(null);
        } else {
            markManualRequired(variant, result.errorMessage());
        }
    }

    private void createAttempt(PublicationVariantEntity variant, PublishResult result) {
        PublicationAttemptEntity attempt = new PublicationAttemptEntity();
        attempt.setPublicationVariant(variant);
        attempt.setAttemptNumber(attemptRepository.countByPublicationVariantId(variant.getId()) + 1);
        attempt.setStatus(result.success() ? PublicationAttemptStatus.SUCCESS : PublicationAttemptStatus.FAILED);
        attempt.setErrorMessage(result.errorMessage());
        attempt.setResponsePayload(result.responsePayload());
        attemptRepository.save(attempt);
    }

    private void markManualRequired(PublicationVariantEntity variant, String reason) {
        variant.setStatus(PublicationVariantStatus.MANUAL_REQUIRED);
        variant.setErrorMessage(reason);
        variant.setManualInstruction(defaultManualInstruction(variant.getPlatform()));
        UserEntity responsible = variant.getContentUnit().getResponsibleUser();
        notificationService.create(
                responsible,
                NotificationType.MANUAL_PUBLICATION_REQUIRED,
                "Публикация требует ручного завершения: " + variant.getPlatform(),
                "/manual-publication/" + variant.getId()
        );
    }

    private void recalculateContentStatus(ContentUnitEntity content) {
        List<PublicationVariantEntity> variants = variantRepository.findByContentUnitIdOrderByPlatformAsc(content.getId());
        if (variants.isEmpty()) {
            return;
        }
        if (variants.stream().allMatch(variant -> FINAL_STATUSES.contains(variant.getStatus()))) {
            content.setStatus(ContentUnitStatus.PUBLISHED);
            return;
        }
        if (variants.stream().allMatch(variant -> variant.getStatus() == PublicationVariantStatus.SCHEDULED)) {
            content.setStatus(ContentUnitStatus.SCHEDULED);
            return;
        }
        if (variants.stream().allMatch(variant -> variant.getStatus() == PublicationVariantStatus.CANCELED)) {
            content.setStatus(ContentUnitStatus.APPROVED);
            return;
        }
        boolean hasCompleted = variants.stream().anyMatch(variant -> FINAL_STATUSES.contains(variant.getStatus()));
        boolean hasActive = variants.stream().anyMatch(variant -> !FINAL_STATUSES.contains(variant.getStatus()) && variant.getStatus() != PublicationVariantStatus.CANCELED);
        if (hasCompleted && hasActive) {
            content.setStatus(ContentUnitStatus.PARTIALLY_PUBLISHED);
        }
    }

    private PublicationVariantEntity findVariant(Long id) {
        return variantRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Publication variant not found"));
    }

    private String defaultManualInstruction(Platform platform) {
        return switch (platform) {
            case TELEGRAM -> "скопируйте текст; скачайте медиа при необходимости; откройте Telegram; вручную опубликуйте пост; вставьте ссылку на пост; завершите manual complete.";
            case VK -> "скопируйте текст; скачайте медиа; откройте VK; вручную опубликуйте пост; вставьте ссылку на пост; завершите manual complete.";
            case TENCHAT -> "скопируйте текст; скачайте медиа; откройте платформу TenChat; вручную опубликуйте пост; вставьте ссылку на пост; завершите manual complete.";
            case SETKA -> "скопируйте текст; скачайте медиа; откройте платформу Setka; вручную опубликуйте пост; вставьте ссылку на пост; завершите manual complete.";
            case MAX -> "скопируйте текст; скачайте медиа; откройте платформу MAX; вручную опубликуйте пост; вставьте ссылку на пост; завершите manual complete.";
            case MASTODON -> "скопируйте текст; скачайте медиа при необходимости; откройте Mastodon instance; вручную опубликуйте пост; вставьте ссылку на post; завершите manual complete.";
            case OTHER -> "скопируйте текст; скачайте медиа; откройте платформу; вручную опубликуйте пост; вставьте ссылку на пост; завершите manual complete.";
        };
    }

    private String platformUrl(PublicationVariantEntity variant) {
        return switch (variant.getPlatform()) {
            case TELEGRAM -> "https://web.telegram.org/";
            case VK -> vkCommunityUrl(platformSettingService.getOrCreate(Platform.VK).getCommunityId());
            case TENCHAT -> "https://tenchat.ru/editor";
            case SETKA -> "https://setka.ru/posts/regular/new";
            case MAX -> maxManualUrl(platformSettingService.getOrCreate(Platform.MAX).getManualUrl());
            case MASTODON -> mastodonManualUrl(platformSettingService.getOrCreate(Platform.MASTODON).getInstanceUrl());
            case OTHER -> "";
        };
    }

    private String mastodonManualUrl(String instanceUrl) {
        return StringUtils.hasText(instanceUrl) ? instanceUrl.trim() : "";
    }

    private String maxManualUrl(String manualUrl) {
        return StringUtils.hasText(manualUrl) ? manualUrl.trim() : "https://web.max.ru/";
    }

    private String vkCommunityUrl(String communityId) {
        if (!StringUtils.hasText(communityId)) {
            return "https://vk.com/";
        }
        String value = communityId.trim();
        if (value.startsWith("https://") || value.startsWith("http://")) {
            return value;
        }
        if (value.matches("^-?\\d+$")) {
            return "https://vk.com/club" + value.replace("-", "");
        }
        if (value.startsWith("@")) {
            return "https://vk.com/" + value.substring(1);
        }
        return "https://vk.com/" + value;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(applicationZoneId);
    }

    private Specification<PublicationVariantEntity> spec(Long contentUnitId, Platform platform, PublicationVariantStatus status, LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (contentUnitId != null) predicates.add(cb.equal(root.get("contentUnit").get("id"), contentUnitId));
            if (platform != null) predicates.add(cb.equal(root.get("platform"), platform));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("scheduledAt"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("scheduledAt"), to));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
