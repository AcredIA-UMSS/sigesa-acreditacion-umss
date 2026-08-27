package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataSubphaseRepository;
import com.umss.sigesa.application.port.out.EvidenceLifecycleQueryPort;
import com.umss.sigesa.domain.model.EvidenceVersionHistoryItem;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EvidenceLifecycleJpaAdapter implements EvidenceLifecycleQueryPort {

    private final EvidenceJpaRepository evidenceRepository;
    private final EvidenceVersionJpaRepository versionRepository;
    private final IndicatorJpaRepository indicatorRepository;
    private final SpringDataSubphaseRepository subphaseRepository;

    public EvidenceLifecycleJpaAdapter(EvidenceJpaRepository evidenceRepository,
                                       EvidenceVersionJpaRepository versionRepository,
                                       IndicatorJpaRepository indicatorRepository,
                                       SpringDataSubphaseRepository subphaseRepository) {
        this.evidenceRepository = evidenceRepository;
        this.versionRepository = versionRepository;
        this.indicatorRepository = indicatorRepository;
        this.subphaseRepository = subphaseRepository;
    }

    @Override
    public Optional<EvidenceContext> findContext(UUID evidenceId) {
        return evidenceRepository.findById(evidenceId)
                .flatMap(this::toContext);
    }

    @Override
    public List<EvidenceVersionHistoryItem> listVersions(UUID evidenceId, UUID latestVersionId) {
        return versionRepository.findByEvidenceIdOrderByVersionNumberDesc(evidenceId).stream()
                .map(version -> toHistoryItem(version, latestVersionId))
                .toList();
    }

    private Optional<EvidenceContext> toContext(EvidenceEntity evidence) {
        UUID programId = resolveProgramId(evidence);
        if (programId == null) {
            return Optional.empty();
        }
        return Optional.of(new EvidenceContext(
                evidence.getId(),
                programId,
                evidence.getLatestVersionId()));
    }

    private UUID resolveProgramId(EvidenceEntity evidence) {
        if (evidence.getIndicatorId() != null) {
            return indicatorRepository.findById(evidence.getIndicatorId())
                    .map(IndicatorEntity::getProgramId)
                    .orElse(null);
        }
        if (evidence.getSubphaseId() != null) {
            return subphaseRepository.findWithProcessById(evidence.getSubphaseId())
                    .map(entity -> entity.getPhase().getProcess().getCareerId())
                    .orElse(null);
        }
        return null;
    }

    private static EvidenceVersionHistoryItem toHistoryItem(
            EvidenceVersionEntity version,
            UUID latestVersionId) {
        Integer supersedes = version.getSupersedesVersionNumber() != null
                ? version.getSupersedesVersionNumber()
                : (version.getVersionNumber() > 1 ? version.getVersionNumber() - 1 : null);
        String filename = version.getOriginalFilename() != null
                ? version.getOriginalFilename()
                : extractFilename(version.getStorageKey());
        return new EvidenceVersionHistoryItem(
                version.getId(),
                version.getVersionNumber(),
                supersedes,
                version.getObservationId(),
                version.getDescription(),
                version.getContentHash(),
                filename,
                version.getCreatedBy(),
                version.getCreatedAt(),
                version.getId().equals(latestVersionId),
                !version.isBlobPurged());
    }

    private static String extractFilename(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return "evidencia";
        }
        int lastUnderscore = storageKey.lastIndexOf('_');
        if (lastUnderscore >= 0 && lastUnderscore < storageKey.length() - 1) {
            return storageKey.substring(lastUnderscore + 1);
        }
        return storageKey;
    }
}
