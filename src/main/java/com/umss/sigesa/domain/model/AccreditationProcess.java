package com.umss.sigesa.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class AccreditationProcess {
    private final UUID id;
    private final UUID templateId;
    private final UUID careerId;
    private final String period;
    private final ProcessType type;
    private final ProcessStatus status;
    private final String taxonomySnapshotVersion;
    private final LocalDateTime createdAt;

    public AccreditationProcess(UUID id, UUID templateId, UUID careerId, String period,
                                ProcessType type, ProcessStatus status,
                                String taxonomySnapshotVersion, LocalDateTime createdAt) {
        this.id = id;
        this.templateId = templateId;
        this.careerId = careerId;
        this.period = period;
        this.type = type;
        this.status = status;
        this.taxonomySnapshotVersion = taxonomySnapshotVersion;
        this.createdAt = createdAt;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getTemplateId() { return templateId; }
    public UUID getCareerId() { return careerId; }
    public String getPeriod() { return period; }
    public ProcessType getType() { return type; }
    public ProcessStatus getStatus() { return status; }
    public String getTaxonomySnapshotVersion() { return taxonomySnapshotVersion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
