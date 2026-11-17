package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.DocumentAssetEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEvidenceLinkEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorObservationJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorStateHistoryEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataDocumentAssetRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataIndicatorEvidenceLinkRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataIndicatorObservationRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataNormativeIndicatorRepository;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import com.umss.sigesa.domain.model.IndicatorEvidenceLink;
import com.umss.sigesa.domain.model.IndicatorObservationStatus;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.IndicatorStateHistoryEntry;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class EvidenceUploadJpaAdapter implements EvidenceUploadPersistencePort {

    private final EvidenceJpaRepository evidenceRepository;
    private final EvidenceVersionJpaRepository versionRepository;
    private final IndicatorStateHistoryJpaRepository historyRepository;
    private final SpringDataIndicatorObservationRepository indicatorObservationRepository;
    private final SpringDataNormativeIndicatorRepository normativeIndicatorRepository;
    private final SpringDataDocumentAssetRepository documentAssetRepository;
    private final SpringDataIndicatorEvidenceLinkRepository linkRepository;

    public EvidenceUploadJpaAdapter(EvidenceJpaRepository evidenceRepository,
                                    EvidenceVersionJpaRepository versionRepository,
                                    IndicatorStateHistoryJpaRepository historyRepository,
                                    SpringDataIndicatorObservationRepository indicatorObservationRepository,
                                    SpringDataNormativeIndicatorRepository normativeIndicatorRepository,
                                    SpringDataDocumentAssetRepository documentAssetRepository,
                                    SpringDataIndicatorEvidenceLinkRepository linkRepository) {
        this.evidenceRepository = evidenceRepository;
        this.versionRepository = versionRepository;
        this.historyRepository = historyRepository;
        this.indicatorObservationRepository = indicatorObservationRepository;
        this.normativeIndicatorRepository = normativeIndicatorRepository;
        this.documentAssetRepository = documentAssetRepository;
        this.linkRepository = linkRepository;
    }

    @Override
    @Transactional
    public void persistUpload(Evidence evidence, EvidenceVersion version, IndicatorStateHistoryEntry historyEntry) {
        persistEvidenceUpload(evidence, version, historyEntry);
    }

    @Override
    @Transactional
    public void persistNormativeIndicatorUpload(Evidence evidence, EvidenceVersion version, String externalUrl) {
        persistNormativeIndicatorUpload(evidence, version, externalUrl, null, false, null);
    }

    @Override
    @Transactional
    public void persistNormativeIndicatorUpload(
            Evidence evidence,
            EvidenceVersion version,
            String externalUrl,
            DocumentAsset documentAsset,
            boolean persistNewAsset,
            IndicatorEvidenceLink link) {
        persistEvidenceUpload(evidence, version, null);
        if (externalUrl != null && !externalUrl.isBlank()) {
            EvidenceVersionEntity versionEntity = versionRepository.findById(version.getId())
                    .orElseThrow(() -> new IllegalStateException("Version not found: " + version.getId()));
            versionEntity.setExternalUrl(externalUrl);
            versionRepository.save(versionEntity);
        }
        if (persistNewAsset && documentAsset != null) {
            documentAssetRepository.save(toAssetEntity(documentAsset));
        }
        if (link != null) {
            linkRepository.save(toLinkEntity(link));
        }
    }

    @Transactional
    public void persistEvidenceUpload(Evidence evidence, EvidenceVersion version,
                                      IndicatorStateHistoryEntry historyEntry) {
        EvidenceEntity evidenceEntity = new EvidenceEntity();
        evidenceEntity.setId(evidence.getId());
        UUID legacyIndicatorId = evidence.getIndicatorId();
        UUID normativeIndicatorId = evidence.getNormativeIndicatorId();
        // Postgres: indicator_id NOT NULL; cargas v2 solo traen normativeIndicatorId.
        if (legacyIndicatorId == null && normativeIndicatorId != null) {
            legacyIndicatorId = normativeIndicatorId;
        }
        evidenceEntity.setIndicatorId(legacyIndicatorId);
        evidenceEntity.setNormativeIndicatorId(normativeIndicatorId);
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
    public String persistNormativeIndicatorSubsanation(UUID evidenceId,
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
        versionEntity.setIndicatorObservationId(observationId);
        versionEntity.setSupersedesVersionNumber(supersedesVersionNumber);
        versionEntity.setBlobPurged(false);
        versionEntity.setOriginalFilename(extractFilename(newVersion.getStorageKey()));
        versionRepository.save(versionEntity);

        evidence.setLatestVersionId(newVersion.getId());
        evidenceRepository.save(evidence);

        superseded.setBlobPurged(true);
        versionRepository.save(superseded);

        IndicatorObservationJpaEntity observation = indicatorObservationRepository.findById(observationId)
                .orElseThrow(() -> new IllegalStateException("Indicator observation not found"));
        observation.setStatus(IndicatorObservationStatus.RESOLVED.name());
        observation.setResolvedAt(LocalDateTime.now());
        observation.setResolvedVersionId(newVersion.getId());
        observation.setUpdatedAt(LocalDateTime.now());
        indicatorObservationRepository.save(observation);

        return superseded.getStorageKey();
    }

    @Override
    @Transactional
    public List<String> retractNormativeEvidence(UUID evidenceId) {
        EvidenceEntity evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalStateException("Evidence not found: " + evidenceId));

        List<EvidenceVersionEntity> versions =
                versionRepository.findByEvidenceIdOrderByVersionNumberDesc(evidenceId);
        Set<UUID> versionIds = versions.stream()
                .map(EvidenceVersionEntity::getId)
                .collect(Collectors.toSet());
        List<String> candidateKeys = versions.stream()
                .map(EvidenceVersionEntity::getStorageKey)
                .filter(key -> key != null && !key.isBlank() && !key.startsWith("external-url:"))
                .distinct()
                .toList();

        UUID indicatorId = evidence.getNormativeIndicatorId();
        if (indicatorId != null) {
            List<IndicatorObservationJpaEntity> observations =
                    indicatorObservationRepository.findByIndicator_Id(indicatorId);
            LocalDateTime now = LocalDateTime.now();
            for (IndicatorObservationJpaEntity observation : observations) {
                if (observation.getResolvedVersionId() != null
                        && versionIds.contains(observation.getResolvedVersionId())) {
                    observation.setResolvedVersionId(null);
                }
                if ("OPEN".equals(observation.getStatus())) {
                    observation.setStatus(IndicatorObservationStatus.RESOLVED.name());
                    observation.setResolvedAt(now);
                }
                observation.setUpdatedAt(now);
            }
            indicatorObservationRepository.saveAll(observations);
        }

        for (EvidenceVersionEntity version : versions) {
            version.setIndicatorObservationId(null);
        }
        versionRepository.saveAll(versions);

        evidence.setLatestVersionId(null);
        evidenceRepository.save(evidence);

        List<IndicatorEvidenceLinkEntity> links = linkRepository.findByEvidenceId(evidenceId);
        Set<UUID> assetIds = links.stream()
                .map(IndicatorEvidenceLinkEntity::getDocumentAssetId)
                .collect(Collectors.toSet());
        linkRepository.deleteByEvidenceId(evidenceId);

        boolean deletedExclusiveAsset = false;
        for (UUID assetId : assetIds) {
            if (linkRepository.findByDocumentAssetId(assetId).isEmpty()) {
                var asset = documentAssetRepository.findById(assetId);
                if (asset.isPresent()) {
                    documentAssetRepository.delete(asset.get());
                    deletedExclusiveAsset = true;
                }
            }
        }

        versionRepository.deleteByEvidenceId(evidenceId);
        evidenceRepository.delete(evidence);

        if (indicatorId != null) {
            normativeIndicatorRepository.findById(indicatorId).ifPresent(indicator -> {
                indicator.setStatus(IndicatorState.PENDIENTE.name());
                indicator.setUpdatedAt(LocalDateTime.now());
                normativeIndicatorRepository.save(indicator);
            });
        }

        boolean keepBlobs = !assetIds.isEmpty() && !deletedExclusiveAsset;
        if (keepBlobs) {
            return List.of();
        }
        return candidateKeys.stream()
                .filter(key -> !versionRepository.existsByStorageKey(key)
                        && !documentAssetRepository.existsByStorageKey(key))
                .toList();
    }

    private static DocumentAssetEntity toAssetEntity(DocumentAsset asset) {
        DocumentAssetEntity entity = new DocumentAssetEntity();
        entity.setId(asset.getId());
        entity.setProcessId(asset.getProcessId());
        entity.setFolioCode(asset.getFolioCode());
        entity.setTitle(asset.getTitle());
        entity.setStorageKey(asset.getStorageKey());
        entity.setContentHash(asset.getContentHash());
        entity.setInstitutional(asset.isInstitutional());
        entity.setUploadedBy(asset.getUploadedBy());
        entity.setCreatedAt(asset.getCreatedAt());
        return entity;
    }

    private static IndicatorEvidenceLinkEntity toLinkEntity(IndicatorEvidenceLink link) {
        IndicatorEvidenceLinkEntity entity = new IndicatorEvidenceLinkEntity();
        entity.setId(link.getId());
        entity.setIndicatorId(link.getIndicatorId());
        entity.setDocumentAssetId(link.getDocumentAssetId());
        entity.setEvidenceId(link.getEvidenceId());
        entity.setPrimaryReference(link.isPrimaryReference());
        entity.setCreatedAt(link.getCreatedAt());
        return entity;
    }

    private static String extractFilename(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return "evidencia";
        }
        int slash = storageKey.lastIndexOf('/');
        if (slash >= 0 && slash < storageKey.length() - 1) {
            return storageKey.substring(slash + 1);
        }
        int underscore = storageKey.indexOf("_v");
        int lastUnderscore = storageKey.lastIndexOf('_');
        if (underscore >= 0 && lastUnderscore > underscore + 2) {
            return storageKey.substring(lastUnderscore + 1);
        }
        return storageKey;
    }
}
