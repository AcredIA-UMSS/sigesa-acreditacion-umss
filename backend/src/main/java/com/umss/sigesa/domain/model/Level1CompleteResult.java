package com.umss.sigesa.domain.model;

import java.util.UUID;

public record Level1CompleteResult(
        UUID level1Id,
        PhaseState previousState,
        PhaseState newState,
        String event
) {
}
