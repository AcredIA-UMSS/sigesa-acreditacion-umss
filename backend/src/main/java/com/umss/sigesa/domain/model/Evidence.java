package com.umss.sigesa.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Evidence {

    private final UUID id;
    private final UUID indicatorId;
    private final UUID subphaseId;
    private UUID latestVersionId;
    private final LocalDateTime createdAt;

    public Evidence(UUID id, UUID indicatorId, UUID subphaseId, UUID latestVersionId, LocalDateTime createdAt) {
        this.id = id;
        this.indicatorId = indicatorId;
        this.subphaseId = subphaseId;
        this.latestVersionId = latestVersionId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getIndicatorId() {
        return indicatorId;
    }

    public UUID getSubphaseId() {
        return subphaseId;
    }

    public UUID getLatestVersionId() {
        return latestVersionId;
    }

    public void setLatestVersionId(UUID latestVersionId) {
        this.latestVersionId = latestVersionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
