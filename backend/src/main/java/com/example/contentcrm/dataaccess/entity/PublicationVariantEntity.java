package com.example.contentcrm.dataaccess.entity;

import com.example.contentcrm.business.model.enums.Platform;
import com.example.contentcrm.business.model.enums.PublicationVariantStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "publication_variants",
        uniqueConstraints = @UniqueConstraint(name = "uq_publication_variant_content_platform", columnNames = {"content_unit_id", "platform"}))
public class PublicationVariantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_unit_id")
    private ContentUnitEntity contentUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(name = "adapted_text")
    private String adaptedText;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublicationVariantStatus status = PublicationVariantStatus.DRAFT;

    @Column(name = "external_post_id")
    private String externalPostId;

    @Column(name = "external_post_url")
    private String externalPostUrl;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "manual_instruction")
    private String manualInstruction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manual_completed_by_id")
    private UserEntity manualCompletedBy;

    @Column(name = "manual_completed_at")
    private LocalDateTime manualCompletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public ContentUnitEntity getContentUnit() {
        return contentUnit;
    }

    public void setContentUnit(ContentUnitEntity contentUnit) {
        this.contentUnit = contentUnit;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public String getAdaptedText() {
        return adaptedText;
    }

    public void setAdaptedText(String adaptedText) {
        this.adaptedText = adaptedText;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public PublicationVariantStatus getStatus() {
        return status;
    }

    public void setStatus(PublicationVariantStatus status) {
        this.status = status;
    }

    public String getExternalPostId() {
        return externalPostId;
    }

    public void setExternalPostId(String externalPostId) {
        this.externalPostId = externalPostId;
    }

    public String getExternalPostUrl() {
        return externalPostUrl;
    }

    public void setExternalPostUrl(String externalPostUrl) {
        this.externalPostUrl = externalPostUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getManualInstruction() {
        return manualInstruction;
    }

    public void setManualInstruction(String manualInstruction) {
        this.manualInstruction = manualInstruction;
    }

    public UserEntity getManualCompletedBy() {
        return manualCompletedBy;
    }

    public void setManualCompletedBy(UserEntity manualCompletedBy) {
        this.manualCompletedBy = manualCompletedBy;
    }

    public LocalDateTime getManualCompletedAt() {
        return manualCompletedAt;
    }

    public void setManualCompletedAt(LocalDateTime manualCompletedAt) {
        this.manualCompletedAt = manualCompletedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
