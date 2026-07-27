package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import com.umss.sigesa.adapter.out.persistance.entity.ObservationEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.config.DevSeedData;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

@Repository
public class EvidenceJpaAdapter implements EvidenceRepositoryPort {

    private final EvidenceJpaRepository evidenceRepository;
    private final EvidenceVersionJpaRepository versionRepository;
    private final ObservationJpaRepository observationRepository;
    private final SpringDataAccreditationProcessRepository processRepository;

    public EvidenceJpaAdapter(EvidenceJpaRepository evidenceRepository,
                              EvidenceVersionJpaRepository versionRepository,
                              ObservationJpaRepository observationRepository,
                              SpringDataAccreditationProcessRepository processRepository) {
        this.evidenceRepository = evidenceRepository;
        this.versionRepository = versionRepository;
        this.observationRepository = observationRepository;
        this.processRepository = processRepository;
    }

    @Override
    @Transactional
    public void save(Evidence evidence, EvidenceVersion version) {
        EvidenceEntity eEntity = new EvidenceEntity();
        eEntity.setId(evidence.getId());
        eEntity.setIndicatorId(evidence.getIndicatorId());
        eEntity.setLatestVersionId(evidence.getLatestVersionId());
        eEntity.setCreatedAt(evidence.getCreatedAt());
        evidenceRepository.save(eEntity);

        EvidenceVersionEntity vEntity = new EvidenceVersionEntity();
        vEntity.setId(version.getId());
        vEntity.setEvidenceId(version.getEvidenceId());
        vEntity.setVersionNumber(version.getVersionNumber());
        vEntity.setDescription(version.getDescription());
        vEntity.setStorageKey(version.getStorageKey());
        vEntity.setContentHash(version.getContentHash());
        vEntity.setObservationId(version.getObservationId());
        vEntity.setCreatedBy(version.getCreatedBy());
        vEntity.setCreatedAt(version.getCreatedAt());
        versionRepository.save(vEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findProgramIdForIndicator(UUID indicatorId) {
        List<ObservationEntity> observations = observationRepository.findAll();
        for (ObservationEntity obs : observations) {
            if (obs.getIndicatorId() != null && obs.getIndicatorId().equals(indicatorId.toString())) {
                return obs.getProgramId();
            }
        }

        // AQUÍ ESTABA EL ERROR: El tipo de la lista debe ser AccreditationProcessJpaEntity, no el Repositorio
        List<AccreditationProcessJpaEntity> activeProcesses = processRepository.findAll().stream()
                .filter(p -> com.umss.sigesa.domain.model.ProcessStatus.ACTIVE.name().equals(p.getStatus()))
                .toList();

        if (!activeProcesses.isEmpty()) {
            return activeProcesses.get(0).getCareerId();
        }

        return DevSeedData.PROGRAM_INF_SIS;
    }
}