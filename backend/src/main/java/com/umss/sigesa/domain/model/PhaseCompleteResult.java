package com.umss.sigesa.domain.model;

import java.util.UUID;

public record PhaseCompleteResult(
        UUID phaseId,
        PhaseState previousState,
        PhaseState newState,
        String event
) {
}
