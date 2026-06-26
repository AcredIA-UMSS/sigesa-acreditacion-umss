package com.umss.sigesa.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class EvidenceVersion {

    private final UUID id;
    private final UUID evidenceId;
    private final int versionNumber;
    private final String contentHash;
    private final UUID criterionId;
    private final String description;
    private final String storageKey;
    private final UUID createdBy;
    private final LocalDateTime createdAt;

    public EvidenceVersion(UUID id,
                           UUID evidenceId,
                           int versionNumber,
                           String contentHash,
                           UUID criterionId,
                           String description,
                           String storageKey,
                           UUID createdBy,
                           LocalDateTime createdAt) {
        this.id = id;
        this.evidenceId = evidenceId;
        this.versionNumber = versionNumber;
        this.contentHash = contentHash;
        this.criterionId = criterionId;
        this.description = description;
        this.storageKey = storageKey;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEvidenceId() {
        return evidenceId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getContentHash() {
        return contentHash;
    }

    public UUID getCriterionId() {
        return criterionId;
    }

    public String getDescription() {
        return description;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
