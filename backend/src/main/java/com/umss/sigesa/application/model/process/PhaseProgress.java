package com.umss.sigesa.application.model.process;

import com.umss.sigesa.domain.model.PhaseState;

import java.util.UUID;

public record PhaseProgress(
        UUID phaseId,
        int totalSubphases,
        int approvedSubphases,
        int pendingSubphases,
        double progressPercent,
        PhaseState state
) {
}
