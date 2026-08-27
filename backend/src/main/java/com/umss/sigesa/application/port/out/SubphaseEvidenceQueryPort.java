package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.SubphaseEvidenceItem;

import java.util.List;
import java.util.UUID;

public interface SubphaseEvidenceQueryPort {

    List<SubphaseEvidenceItem> listBySubphaseId(UUID subphaseId);

    record SubphaseEvidenceRef(
            UUID evidenceId,
            UUID subphaseId,
            UUID indicatorId,
            UUID latestVersionId,
            UUID criterionId,
            int currentVersionNumber) {
    }

    java.util.Optional<SubphaseEvidenceRef> findEvidenceRef(UUID evidenceId, UUID subphaseId);

    boolean hasEvidences(UUID subphaseId);

    List<UUID> findLinkedIndicatorIds(UUID subphaseId);

    boolean hasEvidenceForIndicator(UUID indicatorId);

    List<UUID> findSubphaseIdsByIndicatorId(UUID indicatorId);
}
