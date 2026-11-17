package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.DocumentAssetEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEvidenceLinkEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataDocumentAssetRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataIndicatorEvidenceLinkRepository;
import com.umss.sigesa.application.port.out.EvidenceLookupPort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EvidenceLookupJpaAdapter implements EvidenceLookupPort {

    private final EvidenceJpaRepository evidenceRepository;
    private final IndicatorJpaRepository indicatorRepository;
    private final EvidenceVersionJpaRepository versionRepository;
    private final SpringDataIndicatorEvidenceLinkRepository linkRepository;
    private final SpringDataDocumentAssetRepository assetRepository;

    public EvidenceLookupJpaAdapter(EvidenceJpaRepository evidenceRepository,
                                    IndicatorJpaRepository indicatorRepository,
                                    EvidenceVersionJpaRepository versionRepository,
                                    SpringDataIndicatorEvidenceLinkRepository linkRepository,
                                    SpringDataDocumentAssetRepository assetRepository) {
        this.evidenceRepository = evidenceRepository;
        this.indicatorRepository = indicatorRepository;
        this.versionRepository = versionRepository;
        this.linkRepository = linkRepository;
        this.assetRepository = assetRepository;
    }

    @Override
    public Optional<EvidenceRef> findById(UUID evidenceId) {
        return evidenceRepository.findById(evidenceId).flatMap(this::toRef);
    }

    @Override
    public List<EvidenceRef> listByProgramId(UUID programId) {
        List<EvidenceRef> refs = new ArrayList<>();
        for (IndicatorEntity indicator : indicatorRepository.findByProgramId(programId)) {
            evidenceRepository.findByIndicatorId(indicator.getId())
                    .flatMap(evidence -> toRef(evidence, indicator))
                    .ifPresent(refs::add);
        }
        return List.copyOf(refs);
    }

    private Optional<EvidenceRef> toRef(EvidenceEntity evidence) {
        return indicatorRepository.findById(evidence.getIndicatorId())
                .flatMap(indicator -> toRef(evidence, indicator));
    }

    private Optional<EvidenceRef> toRef(EvidenceEntity evidence, IndicatorEntity indicator) {
        if (!indicator.getId().equals(evidence.getIndicatorId())) {
            return Optional.empty();
        }
        String filename = filenameOf(evidence);
        IndicatorEvidenceLinkEntity link = linkRepository.findFirstByEvidenceId(evidence.getId()).orElse(null);
        UUID documentAssetId = link == null ? null : link.getDocumentAssetId();
        String folioCode = folioOf(documentAssetId);
        return Optional.of(new EvidenceRef(
                evidence.getId(),
                indicator.getId(),
                indicator.getProgramId(),
                indicator.getCode(),
                indicator.getTitle(),
                filename,
                documentAssetId,
                folioCode));
    }

    private String filenameOf(EvidenceEntity evidence) {
        if (evidence.getLatestVersionId() == null) {
            return null;
        }
        return versionRepository.findById(evidence.getLatestVersionId())
                .map(EvidenceVersionEntity::getOriginalFilename)
                .orElse(null);
    }

    private String folioOf(UUID documentAssetId) {
        if (documentAssetId == null) {
            return null;
        }
        return assetRepository.findById(documentAssetId)
                .map(DocumentAssetEntity::getFolioCode)
                .orElse(null);
    }
}
