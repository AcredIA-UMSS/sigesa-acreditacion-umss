package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.in.ListEvidenceVersionsUseCase;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.application.port.out.IndicatorCatalogPort;
import com.umss.sigesa.domain.exception.ForbiddenProgramScopeException;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListEvidenceVersionsService implements ListEvidenceVersionsUseCase {

    private final EvidenceRepositoryPort evidenceRepository;
    private final IndicatorCatalogPort indicatorCatalog;

    public ListEvidenceVersionsService(EvidenceRepositoryPort evidenceRepository,
                                       IndicatorCatalogPort indicatorCatalog) {
        this.evidenceRepository = evidenceRepository;
        this.indicatorCatalog = indicatorCatalog;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceVersionSummary> list(UUID evidenceId, List<UUID> allowedProgramIds) {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidencia no encontrada."));

        UUID programId = evidenceRepository.findProgramIdForIndicator(evidence.getIndicatorId());
        assertProgramAccess(programId, allowedProgramIds);

        List<EvidenceVersion> versions = evidenceRepository.findVersionsByEvidenceId(evidenceId);
        return versions.stream()
                .map(version -> new EvidenceVersionSummary(
                        version.getVersionNumber(),
                        resolveSupersedesId(versions, version.getVersionNumber()),
                        version.getObservationId() != null ? version.getObservationId().toString() : null,
                        version.getCreatedAt(),
                        version.getCreatedBy()
                ))
                .toList();
    }

    private UUID resolveSupersedesId(List<EvidenceVersion> versions, int versionNumber) {
        if (versionNumber <= 1) {
            return null;
        }
        return versions.stream()
                .filter(version -> version.getVersionNumber() == versionNumber - 1)
                .map(EvidenceVersion::getId)
                .findFirst()
                .orElse(null);
    }

    private void assertProgramAccess(UUID programId, List<UUID> allowedProgramIds) {
        if (allowedProgramIds == null || allowedProgramIds.isEmpty()) {
            return;
        }
        if (!allowedProgramIds.contains(programId)) {
            throw new ForbiddenProgramScopeException("No tiene permisos sobre la carrera de esta evidencia.");
        }
    }
}
