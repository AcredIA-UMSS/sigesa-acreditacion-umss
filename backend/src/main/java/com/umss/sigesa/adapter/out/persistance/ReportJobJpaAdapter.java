package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ReportJobEntity;
import com.umss.sigesa.application.port.out.ReportJobRepositoryPort;
import com.umss.sigesa.domain.model.ExecutiveReportFilters;
import com.umss.sigesa.domain.model.ReportJob;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ReportJobJpaAdapter implements ReportJobRepositoryPort {

    private final ReportJobJpaRepository repository;

    public ReportJobJpaAdapter(ReportJobJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ReportJob save(ReportJob job) {
        return toDomain(repository.save(toEntity(job)));
    }

    @Override
    public Optional<ReportJob> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private ReportJobEntity toEntity(ReportJob job) {
        ReportJobEntity entity = new ReportJobEntity();
        entity.setId(job.getId());
        entity.setRequesterId(job.getRequesterId());
        entity.setFacultyId(job.getFilters().facultyId());
        entity.setProgramId(job.getFilters().programId());
        entity.setManagementYear(job.getFilters().managementYear());
        entity.setStatus(job.getStatus());
        entity.setArtifactKey(job.getArtifactKey());
        entity.setErrorCode(job.getErrorCode());
        entity.setCreatedAt(job.getCreatedAt());
        entity.setCompletedAt(job.getCompletedAt());
        return entity;
    }

    private ReportJob toDomain(ReportJobEntity entity) {
        return new ReportJob(
                entity.getId(),
                entity.getRequesterId(),
                new ExecutiveReportFilters(
                        entity.getFacultyId(),
                        entity.getProgramId(),
                        entity.getManagementYear()
                ),
                entity.getStatus(),
                entity.getArtifactKey(),
                entity.getErrorCode(),
                entity.getCreatedAt(),
                entity.getCompletedAt()
        );
    }
}
