package com.umss.sigesa.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReportJob {

    private final UUID id;
    private final UUID requesterId;
    private final ExecutiveReportFilters filters;
    private ReportJobStatus status;
    private String artifactKey;
    private String errorCode;
    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public ReportJob(UUID id,
                     UUID requesterId,
                     ExecutiveReportFilters filters,
                     ReportJobStatus status,
                     String artifactKey,
                     String errorCode,
                     LocalDateTime createdAt,
                     LocalDateTime completedAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.filters = filters;
        this.status = status;
        this.artifactKey = artifactKey;
        this.errorCode = errorCode;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public static ReportJob createPending(UUID id, UUID requesterId, ExecutiveReportFilters filters) {
        return new ReportJob(
                id,
                requesterId,
                filters,
                ReportJobStatus.PENDING,
                null,
                null,
                LocalDateTime.now(),
                null
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getRequesterId() {
        return requesterId;
    }

    public ExecutiveReportFilters getFilters() {
        return filters;
    }

    public ReportJobStatus getStatus() {
        return status;
    }

    public String getArtifactKey() {
        return artifactKey;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void markInProgress() {
        this.status = ReportJobStatus.IN_PROGRESS;
    }

    public void markCompleted(String artifactKey) {
        this.status = ReportJobStatus.COMPLETED;
        this.artifactKey = artifactKey;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed(String errorCode) {
        this.status = ReportJobStatus.FAILED;
        this.errorCode = errorCode;
        this.completedAt = LocalDateTime.now();
    }
}
