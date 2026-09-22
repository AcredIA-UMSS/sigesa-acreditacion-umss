package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceItem;

import java.util.List;
import java.util.UUID;

public interface NormativeIndicatorEvidenceQueryPort {

    record NormativeIndicatorEvidenceRef(
            UUID evidenceId,
            UUID indicatorId,
            UUID latestVersionId,
            int currentVersionNumber) {
    }

    List<NormativeIndicatorEvidenceItem> listByIndicatorId(UUID indicatorId);

    java.util.Optional<NormativeIndicatorEvidenceRef> findEvidenceRef(UUID evidenceId, UUID indicatorId);

    boolean hasEvidences(UUID indicatorId);
}
