package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.application.port.out.IndicatorCatalogPort;
import com.umss.sigesa.config.DevSeedData;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EvidenceJpaAdapter implements EvidenceRepositoryPort {

    private final EvidenceJpaRepository evidenceRepository;
    private final EvidenceVersionJpaRepository versionRepository;
    private final IndicatorCatalogPort indicatorCatalog;

    public EvidenceJpaAdapter(EvidenceJpaRepository evidenceRepository,
                              EvidenceVersionJpaRepository versionRepository,
                              IndicatorCatalogPort indicatorCatalog) {
        this.evidenceRepository = evidenceRepository;
        this.versionRepository = versionRepository;
        this.indicatorCatalog = indicatorCatalog;
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

        EvidenceVersionEntity vEntity = toVersionEntity(version);
        versionRepository.save(vEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findProgramIdForIndicator(UUID indicatorId) {
        return indicatorCatalog.findById(indicatorId)
                .map(IndicatorCatalogPort.IndicatorEntry::programId)
                .orElse(DevSeedData.PROGRAM_INF_SIS);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Evidence> findById(UUID evidenceId) {
        return evidenceRepository.findById(evidenceId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Evidence> findByIndicatorId(UUID indicatorId) {
        return evidenceRepository.findByIndicatorId(indicatorId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Evidence> findAll() {
        return evidenceRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceVersion> findVersionsByEvidenceId(UUID evidenceId) {
        return versionRepository.findByEvidenceIdOrderByVersionNumberAsc(evidenceId).stream()
                .map(this::toVersionDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EvidenceVersion> findVersionById(UUID versionId) {
        return versionRepository.findById(versionId).map(this::toVersionDomain);
    }

    private Evidence toDomain(EvidenceEntity entity) {
        return new Evidence(
                entity.getId(),
                entity.getIndicatorId(),
                entity.getLatestVersionId(),
                entity.getCreatedAt()
        );
    }

    private EvidenceVersion toVersionDomain(EvidenceVersionEntity entity) {
        return new EvidenceVersion(
                entity.getId(),
                entity.getEvidenceId(),
                entity.getVersionNumber(),
                entity.getDescription(),
                entity.getStorageKey(),
                entity.getContentHash(),
                entity.getObservationId(),
                entity.getCreatedBy(),
                entity.getCreatedAt()
        );
    }

    private EvidenceVersionEntity toVersionEntity(EvidenceVersion version) {
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
        return vEntity;
    }
}
