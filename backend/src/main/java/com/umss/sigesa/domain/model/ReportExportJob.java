package com.umss.sigesa.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ReportExportJob {

    private final UUID jobId;
    private final UUID userId;
    private final UUID programId;
    private final ReportFormat format;
    private final Integer phaseId;
    private JobStatus status;
    private int progressPercentage;
    private String filePath;
    private String errorMessage;
    private final Instant createdAt;
    private Instant updatedAt;

    public ReportExportJob(UUID jobId, UUID userId, UUID programId, ReportFormat format, Integer phaseId,
                           JobStatus status, int progressPercentage, String filePath, String errorMessage,
                           Instant createdAt, Instant updatedAt) {
        this.jobId = Objects.requireNonNull(jobId, "jobId is required");
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.programId = Objects.requireNonNull(programId, "programId is required");
        this.format = Objects.requireNonNull(format, "format is required");
        this.phaseId = phaseId;
        this.status = Objects.requireNonNull(status, "status is required");
        this.progressPercentage = progressPercentage;
        this.filePath = filePath;
        this.errorMessage = errorMessage;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static ReportExportJob createNew(UUID userId, UUID programId, ReportFormat format, Integer phaseId) {
        Instant now = Instant.now();
        return new ReportExportJob(
                UUID.randomUUID(),
                userId,
                programId,
                format,
                phaseId,
                JobStatus.PENDING,
                0,
                null,
                null,
                now,
                now
        );
    }

    public void markProcessing() {
        this.status = JobStatus.PROCESSING;
        this.progressPercentage = 10;
        this.updatedAt = Instant.now();
    }

    public void updateProgress(int percentage) {
        this.progressPercentage = Math.min(100, Math.max(0, percentage));
        this.updatedAt = Instant.now();
    }

    public void markCompleted(String filePath) {
        this.status = JobStatus.COMPLETED;
        this.progressPercentage = 100;
        this.filePath = filePath;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = Instant.now();
    }

    public UUID getJobId() { return jobId; }
    public UUID getUserId() { return userId; }
    public UUID getProgramId() { return programId; }
    public ReportFormat getFormat() { return format; }
    public Integer getPhaseId() { return phaseId; }
    public JobStatus getStatus() { return status; }
    public int getProgressPercentage() { return progressPercentage; }
    public String getFilePath() { return filePath; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
