package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvidenceRepositoryPort {
    void save(Evidence evidence, EvidenceVersion version);

    UUID findProgramIdForIndicator(UUID indicatorId);

    Optional<Evidence> findById(UUID evidenceId);

    List<Evidence> findAll();

    List<EvidenceVersion> findVersionsByEvidenceId(UUID evidenceId);

    Optional<EvidenceVersion> findVersionById(UUID versionId);
}
