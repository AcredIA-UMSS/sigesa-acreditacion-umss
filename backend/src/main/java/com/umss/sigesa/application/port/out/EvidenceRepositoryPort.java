package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;

import java.util.UUID;

public interface EvidenceRepositoryPort {

    boolean existsByIndicatorId(UUID indicatorId);

    SavedEvidence saveEvidenceWithVersion(Evidence evidence, EvidenceVersion version);

    record SavedEvidence(Evidence evidence, EvidenceVersion version) {
    }
}
