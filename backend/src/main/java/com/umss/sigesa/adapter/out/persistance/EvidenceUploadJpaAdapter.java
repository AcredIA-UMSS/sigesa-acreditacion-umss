package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorStateHistoryEntity;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import com.umss.sigesa.domain.model.IndicatorStateHistoryEntry;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class EvidenceUploadJpaAdapter implements EvidenceUploadPersistencePort {

    private final EvidenceJpaRepository evidenceRepository;
    private final EvidenceVersionJpaRepository versionRepository;
    private final IndicatorStateHistoryJpaRepository historyRepository;

    public EvidenceUploadJpaAdapter(EvidenceJpaRepository evidenceRepository,
                                      EvidenceVersionJpaRepository versionRepository,
                                      IndicatorStateHistoryJpaRepository historyRepository) {
        this.evidenceRepository = evidenceRepository;
        this.versionRepository = versionRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    @Transactional
    public void persistUpload(Evidence evidence, EvidenceVersion version, IndicatorStateHistoryEntry historyEntry) {
        EvidenceEntity evidenceEntity = new EvidenceEntity();
        evidenceEntity.setId(evidence.getId());
        evidenceEntity.setIndicatorId(evidence.getIndicatorId());
        evidenceEntity.setLatestVersionId(version.getId());
        evidenceEntity.setCreatedAt(evidence.getCreatedAt());
        evidenceRepository.save(evidenceEntity);

        EvidenceVersionEntity versionEntity = new EvidenceVersionEntity();
        versionEntity.setId(version.getId());
        versionEntity.setEvidenceId(version.getEvidenceId());
        versionEntity.setVersionNumber(version.getVersionNumber());
        versionEntity.setContentHash(version.getContentHash());
        versionEntity.setCriterionId(version.getCriterionId());
        versionEntity.setDescription(version.getDescription());
        versionEntity.setStorageKey(version.getStorageKey());
        versionEntity.setCreatedBy(version.getCreatedBy());
        versionEntity.setCreatedAt(version.getCreatedAt());
        versionRepository.save(versionEntity);

        IndicatorStateHistoryEntity historyEntity = new IndicatorStateHistoryEntity();
        historyEntity.setId(historyEntry.id());
        historyEntity.setIndicatorId(historyEntry.indicatorId());
        historyEntity.setPreviousState(historyEntry.previousState());
        historyEntity.setNewState(historyEntry.newState());
        historyEntity.setActorId(historyEntry.actorId());
        historyEntity.setActorRole(historyEntry.actorRole());
        historyEntity.setCreatedAt(historyEntry.createdAt());
        historyRepository.save(historyEntity);
    }
}
