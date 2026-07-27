package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public class EvidenceJpaAdapter implements EvidenceRepositoryPort {

    private final EvidenceJpaRepository evidenceRepository;
    private final EvidenceVersionJpaRepository versionRepository;

    public EvidenceJpaAdapter(EvidenceJpaRepository evidenceRepository,
                              EvidenceVersionJpaRepository versionRepository) {
        this.evidenceRepository = evidenceRepository;
        this.versionRepository = versionRepository;
    }

    @Override
    public boolean existsByIndicatorId(UUID indicatorId) {
        return evidenceRepository.existsByIndicatorId(indicatorId);
    }

    @Override
    @Transactional
    public SavedEvidence saveEvidenceWithVersion(Evidence evidence, EvidenceVersion version) {
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

        return new SavedEvidence(evidence, version);
    }
}
