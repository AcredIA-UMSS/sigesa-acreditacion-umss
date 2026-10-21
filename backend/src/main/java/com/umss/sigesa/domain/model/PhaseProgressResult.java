package com.umss.sigesa.domain.model;

import java.util.UUID;

public record PhaseProgressResult(
        UUID phaseId,
        int totalSubphases,
        int approvedSubphases,
        int pendingSubphases,
        double progressPercent,
        PhaseState state
) {
}
