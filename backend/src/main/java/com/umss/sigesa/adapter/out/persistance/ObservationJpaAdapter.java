package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ObservationEntity;
import com.umss.sigesa.application.port.out.ObservationRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class ObservationJpaAdapter implements ObservationRepositoryPort {

    private final ObservationJpaRepository repository;

    public ObservationJpaAdapter(ObservationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void savePendingObservation(PendingObservation observation) {
        ObservationEntity entity = new ObservationEntity();
        
        UUID id;
        try {
            id = UUID.fromString(observation.observationId());
        } catch (IllegalArgumentException e) {
            id = UUID.nameUUIDFromBytes(observation.observationId().getBytes());
        }
        
        entity.setId(id);
        entity.setEvidenceVersionId(observation.evidenceVersionId());
        entity.setObserverId(observation.observerId());
        entity.setRoleCode(observation.roleCode());
        entity.setObservations(observation.description());
        entity.setCreatedAt(observation.issueDate().atStartOfDay());
        
        entity.setProgramId(observation.programId());
        entity.setIndicatorId(observation.indicatorId());
        entity.setIndicatorCode(observation.indicatorCode());
        entity.setIndicatorTitle(observation.indicatorTitle());
        entity.setDueDate(observation.dueDate());
        entity.setStatus(observation.status());
        entity.setRemediationUrl(observation.remediationUrl());
        
        repository.save(entity);
    }

    @Override
    @Transactional
    public int resolveObservationForIndicator(UUID programId, String indicatorId, String status) {
        List<ObservationEntity> observations = repository.findByProgramIdAndIndicatorIdAndStatus(
                programId, indicatorId, "PENDING_REMEDIATION");
        for (ObservationEntity obs : observations) {
            obs.setStatus(status);
            repository.save(obs);
        }
        return observations.size();
    }

    @Override
    @Transactional
    public void transitionObservationStatus(UUID programId, String indicatorId, String oldStatus, String newStatus) {
        List<ObservationEntity> observations = repository.findByProgramIdAndIndicatorIdAndStatus(
                programId, indicatorId, oldStatus);
        for (ObservationEntity obs : observations) {
            obs.setStatus(newStatus);
            repository.save(obs);
        }
    }
}
