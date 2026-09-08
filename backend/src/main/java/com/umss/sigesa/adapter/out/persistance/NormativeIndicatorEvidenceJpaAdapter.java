package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import com.umss.sigesa.application.port.out.NormativeIndicatorEvidenceQueryPort;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceItem;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NormativeIndicatorEvidenceJpaAdapter implements NormativeIndicatorEvidenceQueryPort {

    private final EvidenceJpaRepository evidenceRepository;
    private final EvidenceVersionJpaRepository evidenceVersionRepository;

    public NormativeIndicatorEvidenceJpaAdapter(
            EvidenceJpaRepository evidenceRepository,
            EvidenceVersionJpaRepository evidenceVersionRepository) {
        this.evidenceRepository = evidenceRepository;
        this.evidenceVersionRepository = evidenceVersionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NormativeIndicatorEvidenceItem> listByIndicatorId(UUID indicatorId) {
        return evidenceRepository.findByNormativeIndicatorIdOrderByCreatedAtDesc(indicatorId).stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NormativeIndicatorEvidenceQueryPort.NormativeIndicatorEvidenceRef> findEvidenceRef(
            UUID evidenceId,
            UUID indicatorId) {
        return evidenceRepository.findById(evidenceId)
                .filter(entity -> indicatorId.equals(entity.getNormativeIndicatorId()))
                .flatMap(entity -> evidenceVersionRepository.findById(entity.getLatestVersionId())
                        .map(version -> new NormativeIndicatorEvidenceQueryPort.NormativeIndicatorEvidenceRef(
                                entity.getId(),
                                entity.getNormativeIndicatorId(),
                                entity.getLatestVersionId(),
                                version.getVersionNumber())));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEvidences(UUID indicatorId) {
        return evidenceRepository.countByNormativeIndicatorId(indicatorId) > 0;
    }

    private NormativeIndicatorEvidenceItem toItem(EvidenceEntity evidence) {
        EvidenceVersionEntity version = evidenceVersionRepository.findById(evidence.getLatestVersionId())
                .orElseThrow();
        return new NormativeIndicatorEvidenceItem(
                evidence.getId(),
                evidence.getNormativeIndicatorId(),
                version.getVersionNumber(),
                version.getDescription(),
                version.getContentHash(),
                extractFilename(version),
                version.getExternalUrl(),
                version.getCreatedAt(),
                version.getCreatedBy());
    }

    private static String extractFilename(EvidenceVersionEntity version) {
        if (version.getOriginalFilename() != null && !version.getOriginalFilename().isBlank()) {
            return version.getOriginalFilename();
        }
        if (version.getExternalUrl() != null && !version.getExternalUrl().isBlank()) {
            return version.getExternalUrl();
        }
        String storageKey = version.getStorageKey();
        if (storageKey == null || storageKey.isBlank()) {
            return "evidencia";
        }
        int slash = storageKey.lastIndexOf('/');
        return slash >= 0 ? storageKey.substring(slash + 1) : storageKey;
    }
}
