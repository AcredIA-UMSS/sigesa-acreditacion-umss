package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorStateHistoryEntity;
import com.umss.sigesa.adapter.out.persistance.entity.SubphaseObservationEntity;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import com.umss.sigesa.domain.model.IndicatorStateHistoryEntry;
import com.umss.sigesa.domain.model.SubphaseObservationStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class EvidenceUploadJpaAdapter implements EvidenceUploadPersistencePort {

    private final EvidenceJpaRepository evidenceRepository;
    private final EvidenceVersionJpaRepository versionRepository;
    private final IndicatorStateHistoryJpaRepository historyRepository;
    private final SubphaseObservationJpaRepository observationRepository;

    public EvidenceUploadJpaAdapter(EvidenceJpaRepository evidenceRepository,
                                      EvidenceVersionJpaRepository versionRepository,
                                      IndicatorStateHistoryJpaRepository historyRepository,
                                      SubphaseObservationJpaRepository observationRepository) {
        this.evidenceRepository = evidenceRepository;
        this.versionRepository = versionRepository;
        this.historyRepository = historyRepository;
        this.observationRepository = observationRepository;
    }

    @Override
    @Transactional
    public void persistUpload(Evidence evidence, EvidenceVersion version, IndicatorStateHistoryEntry historyEntry) {
        persistSubphaseUpload(evidence, version, historyEntry);
    }

    @Override
    @Transactional
    public void persistSubphaseUpload(Evidence evidence, EvidenceVersion version) {
        persistSubphaseUpload(evidence, version, null);
    }

    @Transactional
    public void persistSubphaseUpload(Evidence evidence, EvidenceVersion version,
                                      IndicatorStateHistoryEntry historyEntry) {
        EvidenceEntity evidenceEntity = new EvidenceEntity();
        evidenceEntity.setId(evidence.getId());
        evidenceEntity.setIndicatorId(evidence.getIndicatorId());
        evidenceEntity.setSubphaseId(evidence.getSubphaseId());
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
        versionEntity.setBlobPurged(false);
        versionEntity.setOriginalFilename(extractFilename(version.getStorageKey()));
        versionRepository.save(versionEntity);

        if (historyEntry != null) {
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

    @Override
    @Transactional
    public String persistSubphaseSubsanation(UUID evidenceId,
                                             EvidenceVersion newVersion,
                                             UUID observationId,
                                             int supersedesVersionNumber,
                                             UUID supersededVersionId) {
        EvidenceEntity evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalStateException("Evidence not found: " + evidenceId));
        EvidenceVersionEntity superseded = versionRepository.findById(supersededVersionId)
                .orElseThrow(() -> new IllegalStateException("Superseded version not found"));

        EvidenceVersionEntity versionEntity = new EvidenceVersionEntity();
        versionEntity.setId(newVersion.getId());
        versionEntity.setEvidenceId(newVersion.getEvidenceId());
        versionEntity.setVersionNumber(newVersion.getVersionNumber());
        versionEntity.setContentHash(newVersion.getContentHash());
        versionEntity.setCriterionId(newVersion.getCriterionId());
        versionEntity.setDescription(newVersion.getDescription());
        versionEntity.setStorageKey(newVersion.getStorageKey());
        versionEntity.setCreatedBy(newVersion.getCreatedBy());
        versionEntity.setCreatedAt(newVersion.getCreatedAt());
        versionEntity.setObservationId(observationId);
        versionEntity.setSupersedesVersionNumber(supersedesVersionNumber);
        versionEntity.setBlobPurged(false);
        versionEntity.setOriginalFilename(extractFilename(newVersion.getStorageKey()));
        versionRepository.save(versionEntity);

        evidence.setLatestVersionId(newVersion.getId());
        evidenceRepository.save(evidence);

        superseded.setBlobPurged(true);
        versionRepository.save(superseded);

        SubphaseObservationEntity observation = observationRepository.findById(observationId)
                .orElseThrow(() -> new IllegalStateException("Observation not found"));
        observation.setStatus(SubphaseObservationStatus.RESOLVED.name());
        observation.setResolvedAt(LocalDateTime.now());
        observation.setResolvedVersionId(newVersion.getId());
        observation.setUpdatedAt(LocalDateTime.now());
        observationRepository.save(observation);

        return superseded.getStorageKey();
    }

    private static String extractFilename(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return "evidencia";
        }
        int underscore = storageKey.indexOf("_v");
        int lastUnderscore = storageKey.lastIndexOf('_');
        if (underscore >= 0 && lastUnderscore > underscore + 2) {
            return storageKey.substring(lastUnderscore + 1);
        }
        return storageKey;
    }
}
