package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessEntity;
import com.umss.sigesa.application.port.out.AccreditationProcessRepositoryPort;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.ProcessStatus;
import com.umss.sigesa.domain.model.ProcessType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccreditationProcessJpaAdapter implements AccreditationProcessRepositoryPort {

    private final AccreditationProcessJpaRepository jpaRepository;

    public AccreditationProcessJpaAdapter(AccreditationProcessJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsActiveProcessByCareerAndTypeAndPeriod(UUID careerId, ProcessType type, String period) {
        return jpaRepository.existsByCareerIdAndTypeAndPeriodAndStatus(
                careerId, type, period, ProcessStatus.ACTIVE);
    }

    @Override
    public AccreditationProcess save(AccreditationProcess process) {
        AccreditationProcessEntity saved = jpaRepository.save(toEntity(process));
        return toDomain(saved);
    }

    @Override
    public List<AccreditationProcess> findAll(ProcessStatus status, UUID careerId, String period) {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .filter(process -> status == null || process.getStatus() == status)
                .filter(process -> careerId == null || process.getCareerId().equals(careerId))
                .filter(process -> period == null || period.isBlank() || process.getPeriod().equals(period))
                .toList();
    }

    @Override
    public Optional<AccreditationProcess> findById(UUID processId) {
        return jpaRepository.findById(processId).map(this::toDomain);
    }

    private AccreditationProcessEntity toEntity(AccreditationProcess process) {
        AccreditationProcessEntity entity = new AccreditationProcessEntity();
        entity.setId(process.getId());
        entity.setTemplateId(process.getTemplateId());
        entity.setCareerId(process.getCareerId());
        entity.setPeriod(process.getPeriod());
        entity.setType(process.getType());
        entity.setStatus(process.getStatus());
        entity.setTaxonomySnapshotVersion(process.getTaxonomySnapshotVersion());
        entity.setCreatedAt(process.getCreatedAt());
        return entity;
    }

    private AccreditationProcess toDomain(AccreditationProcessEntity entity) {
        return new AccreditationProcess(
                entity.getId(),
                entity.getTemplateId(),
                entity.getCareerId(),
                entity.getPeriod(),
                entity.getType(),
                entity.getStatus(),
                entity.getTaxonomySnapshotVersion(),
                entity.getCreatedAt()
        );
    }
}
