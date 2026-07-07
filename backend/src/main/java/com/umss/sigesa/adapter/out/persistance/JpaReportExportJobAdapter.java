package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ReportExportJobEntity;
import com.umss.sigesa.application.port.out.ReportExportJobRepositoryPort;
import com.umss.sigesa.domain.model.ReportExportJob;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JpaReportExportJobAdapter implements ReportExportJobRepositoryPort {

    private final ReportExportJobJpaRepository repository;

    public JpaReportExportJobAdapter(ReportExportJobJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ReportExportJob save(ReportExportJob job) {
        ReportExportJobEntity entity = toEntity(job);
        ReportExportJobEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ReportExportJob> findById(UUID jobId) {
        return repository.findById(jobId).map(this::toDomain);
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
