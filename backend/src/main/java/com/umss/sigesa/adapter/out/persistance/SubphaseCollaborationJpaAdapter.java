package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import com.umss.sigesa.adapter.out.persistance.entity.SubphaseJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.SubphaseObservationEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataSubphaseRepository;
import com.umss.sigesa.application.port.out.SubphaseEvidenceQueryPort;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.domain.model.SubphaseEvidenceItem;
import com.umss.sigesa.domain.model.SubphaseObservation;
import com.umss.sigesa.domain.model.SubphaseObservationStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SubphaseCollaborationJpaAdapter implements SubphaseQueryPort, SubphaseEvidenceQueryPort,
        SubphaseObservationPort {

    private final SpringDataSubphaseRepository subphaseRepository;
    private final EvidenceJpaRepository evidenceRepository;
    private final EvidenceVersionJpaRepository evidenceVersionRepository;
    private final SubphaseObservationJpaRepository observationRepository;

    public SubphaseCollaborationJpaAdapter(SpringDataSubphaseRepository subphaseRepository,
                                             EvidenceJpaRepository evidenceRepository,
                                             EvidenceVersionJpaRepository evidenceVersionRepository,
                                             SubphaseObservationJpaRepository observationRepository) {
        this.subphaseRepository = subphaseRepository;
        this.evidenceRepository = evidenceRepository;
        this.evidenceVersionRepository = evidenceVersionRepository;
        this.observationRepository = observationRepository;
    }

    @Override
    public Optional<SubphaseContext> findContext(UUID subphaseId) {
        return subphaseRepository.findWithProcessById(subphaseId)
                .map(entity -> new SubphaseContext(
                        entity.getId(),
                        entity.getPhase().getProcess().getCareerId(),
                        entity.getName()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubphaseEvidenceItem> listBySubphaseId(UUID subphaseId) {
        return evidenceRepository.findBySubphaseIdOrderByCreatedAtDesc(subphaseId).stream()
                .map(this::toEvidenceItem)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubphaseEvidenceRef> findEvidenceRef(UUID evidenceId, UUID subphaseId) {
        return evidenceRepository.findById(evidenceId)
                .filter(entity -> subphaseId.equals(entity.getSubphaseId()))
                .flatMap(entity -> evidenceVersionRepository.findById(entity.getLatestVersionId())
                        .map(version -> new SubphaseEvidenceRef(
                                entity.getId(),
                                entity.getSubphaseId(),
                                entity.getIndicatorId(),
                                entity.getLatestVersionId(),
                                version.getCriterionId(),
                                version.getVersionNumber())));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEvidences(UUID subphaseId) {
        return evidenceRepository.countBySubphaseId(subphaseId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findLinkedIndicatorIds(UUID subphaseId) {
        return evidenceRepository.findDistinctIndicatorIdsBySubphaseId(subphaseId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEvidenceForIndicator(UUID indicatorId) {
        return evidenceRepository.existsByIndicatorId(indicatorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findSubphaseIdsByIndicatorId(UUID indicatorId) {
        return evidenceRepository.findDistinctSubphaseIdsByIndicatorId(indicatorId);
    }

    @Override
    @Transactional
    public SubphaseObservation save(SubphaseObservation observation) {
        SubphaseObservationEntity entity = new SubphaseObservationEntity();
        entity.setId(observation.getId());
        entity.setSubphaseId(observation.getSubphaseId());
        entity.setAuthorId(observation.getAuthorId());
        entity.setAuthorRole(observation.getAuthorRole());
        entity.setBody(observation.getBody());
        entity.setStatus(observation.getStatus() != null
                ? observation.getStatus().name()
                : SubphaseObservationStatus.OPEN.name());
        entity.setCreatedAt(observation.getCreatedAt());
        entity.setUpdatedAt(observation.getUpdatedAt());
        SubphaseObservationEntity saved = observationRepository.save(entity);
        return toObservationDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubphaseObservation> findBySubphaseId(UUID subphaseId) {
        return observationRepository.findBySubphaseIdOrderByCreatedAtDesc(subphaseId).stream()
                .map(SubphaseCollaborationJpaAdapter::toObservationDomain)
                .toList();
    }

    private SubphaseEvidenceItem toEvidenceItem(EvidenceEntity evidence) {
        EvidenceVersionEntity version = evidenceVersionRepository.findById(evidence.getLatestVersionId())
                .orElseThrow();
        String filename = extractFilename(version.getStorageKey());
        return new SubphaseEvidenceItem(
                evidence.getId(),
                evidence.getSubphaseId(),
                evidence.getIndicatorId(),
                version.getVersionNumber(),
                version.getDescription(),
                version.getContentHash(),
                filename,
                version.getCreatedAt(),
                version.getCreatedBy());
    }

    @Override
    public Optional<SubphaseObservation> findLatestOpenBySubphaseId(UUID subphaseId) {
        return observationRepository
                .findFirstBySubphaseIdAndStatusOrderByCreatedAtDesc(subphaseId, SubphaseObservationStatus.OPEN.name())
                .map(SubphaseCollaborationJpaAdapter::toObservationDomain);
    }

    @Override
    @Transactional
    public SubphaseObservation resolve(UUID observationId, UUID resolvedVersionId) {
        SubphaseObservationEntity entity = observationRepository.findById(observationId)
                .orElseThrow(() -> new IllegalStateException("Observation not found"));
        entity.setStatus(SubphaseObservationStatus.RESOLVED.name());
        entity.setResolvedAt(java.time.LocalDateTime.now());
        entity.setResolvedVersionId(resolvedVersionId);
        entity.setUpdatedAt(java.time.LocalDateTime.now());
        return toObservationDomain(observationRepository.save(entity));
    }

    private static String extractFilename(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return "evidencia";
        }
        int slash = storageKey.lastIndexOf('/');
        return slash >= 0 ? storageKey.substring(slash + 1) : storageKey;
    }

    private static SubphaseObservation toObservationDomain(SubphaseObservationEntity entity) {
        return SubphaseObservation.builder()
                .id(entity.getId())
                .subphaseId(entity.getSubphaseId())
                .authorId(entity.getAuthorId())
                .authorRole(entity.getAuthorRole())
                .body(entity.getBody())
                .status(entity.getStatus() != null
                        ? SubphaseObservationStatus.valueOf(entity.getStatus())
                        : SubphaseObservationStatus.OPEN)
                .resolvedAt(entity.getResolvedAt())
                .resolvedVersionId(entity.getResolvedVersionId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
