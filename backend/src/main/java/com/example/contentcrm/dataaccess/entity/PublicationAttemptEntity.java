package com.example.contentcrm.dataaccess.entity;

import com.example.contentcrm.business.model.enums.PublicationAttemptStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "publication_attempts",
        uniqueConstraints = @UniqueConstraint(name = "uq_publication_attempt_number", columnNames = {"publication_variant_id", "attempt_number"}))
public class PublicationAttemptEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publication_variant_id")
    private PublicationVariantEntity publicationVariant;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublicationAttemptStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "response_payload")
    private String responsePayload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public PublicationVariantEntity getPublicationVariant() {
        return publicationVariant;
    }

    public void setPublicationVariant(PublicationVariantEntity publicationVariant) {
        this.publicationVariant = publicationVariant;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public PublicationAttemptStatus getStatus() {
        return status;
    }

    public void setStatus(PublicationAttemptStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
