package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ReportExportJobEntity;
import com.umss.sigesa.application.port.out.ReportExportJobRepositoryPort;
import com.umss.sigesa.domain.model.JobStatus;
import com.umss.sigesa.domain.model.ReportExportJob;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaReportExportJobAdapter implements ReportExportJobRepositoryPort {

    private final ReportExportJobJpaRepository repository;

    public JpaReportExportJobAdapter(ReportExportJobJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ReportExportJob save(ReportExportJob job) {
        ReportExportJobEntity entity = toEntity(job);
        ReportExportJobEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReportExportJob> findById(UUID jobId) {
        return repository.findById(jobId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveJobForUser(UUID userId) {
        if (userId == null) {
            return false;
        }
        return repository.countByUserIdAndStatusIn(userId, List.of(JobStatus.PENDING, JobStatus.PROCESSING)) > 0;
    }

    @Override
    @Transactional
    public int markStaleJobsAsFailed(java.time.Duration maxStaleDuration, String reason) {
        java.time.Instant threshold = java.time.Instant.now().minus(maxStaleDuration);
        return repository.markStaleJobsAsFailed(
                List.of(JobStatus.PENDING, JobStatus.PROCESSING),
                threshold,
                JobStatus.FAILED,
                reason,
                java.time.Instant.now()
        );
    }

    private ReportExportJobEntity toEntity(ReportExportJob job) {
        ReportExportJobEntity entity = new ReportExportJobEntity();
        entity.setJobId(job.getJobId());
        entity.setUserId(job.getUserId());
        entity.setProgramId(job.getProgramId());
        entity.setFormat(job.getFormat());
        entity.setPhaseId(job.getPhaseId());
        entity.setStatus(job.getStatus());
        entity.setProgressPercentage(job.getProgressPercentage());
        entity.setFilePath(job.getFilePath());
        entity.setErrorMessage(job.getErrorMessage());
        entity.setCreatedAt(job.getCreatedAt());
        entity.setUpdatedAt(job.getUpdatedAt());
        return entity;
    }

    private ReportExportJob toDomain(ReportExportJobEntity entity) {
        return new ReportExportJob(
                entity.getJobId(),
                entity.getUserId(),
                entity.getProgramId(),
                entity.getFormat(),
                entity.getPhaseId(),
                entity.getStatus(),
                entity.getProgressPercentage(),
                entity.getFilePath(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
