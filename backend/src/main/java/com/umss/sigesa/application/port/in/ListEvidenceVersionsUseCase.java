package com.umss.sigesa.application.port.in;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ListEvidenceVersionsUseCase {

    List<EvidenceVersionSummary> list(UUID evidenceId, List<UUID> allowedProgramIds);

    record EvidenceVersionSummary(
            int version,
            UUID supersedesId,
            String observationId,
            LocalDateTime createdAt,
            UUID createdBy
    ) {
    }
}
