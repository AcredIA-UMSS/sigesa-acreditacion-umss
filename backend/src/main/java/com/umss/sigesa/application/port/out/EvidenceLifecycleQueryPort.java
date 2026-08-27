package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.EvidenceVersionHistoryItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvidenceLifecycleQueryPort {

    record EvidenceContext(UUID evidenceId, UUID programId, UUID latestVersionId) {
    }

    Optional<EvidenceContext> findContext(UUID evidenceId);

    List<EvidenceVersionHistoryItem> listVersions(UUID evidenceId, UUID latestVersionId);
}
