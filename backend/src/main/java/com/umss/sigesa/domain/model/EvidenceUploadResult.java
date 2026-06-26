package com.umss.sigesa.domain.model;

import java.util.UUID;

public record EvidenceUploadResult(
        UUID evidenceId,
        int version,
        String contentHash,
        String event,
        IndicatorState currentState
) {
}
