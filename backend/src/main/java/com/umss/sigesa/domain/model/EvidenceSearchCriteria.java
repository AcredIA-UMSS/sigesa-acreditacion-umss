package com.umss.sigesa.domain.model;

import java.util.UUID;

public record EvidenceSearchCriteria(
        UUID processId,
        UUID phaseId,
        UUID subphaseId,
        UUID indicatorId,
        UUID programId,
        String query,
        Integer managementYear,
        int page,
        int size
) {
    public EvidenceSearchCriteria {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 100) {
            size = 20;
        }
    }
}
