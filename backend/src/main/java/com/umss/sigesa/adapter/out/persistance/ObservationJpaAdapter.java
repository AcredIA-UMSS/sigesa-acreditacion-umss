package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ObservationEntity;
import com.umss.sigesa.application.port.out.ObservationRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
        entity.setObservationId(observation.observationId());
        entity.setProgramId(observation.programId());
        entity.setIndicatorId(observation.indicatorId());
        entity.setIndicatorCode(observation.indicatorCode());
        entity.setIndicatorTitle(observation.indicatorTitle());
        entity.setDescription(observation.description());
        entity.setIssueDate(observation.issueDate());
        entity.setDueDate(observation.dueDate());
        entity.setPhaseId(observation.phaseId());
        entity.setStatus(observation.status());
        entity.setRemediationUrl(observation.remediationUrl());
        repository.save(entity);
    }
}
