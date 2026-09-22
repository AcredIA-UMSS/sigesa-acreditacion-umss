package com.umss.sigesa.domain.model;

import java.util.UUID;

public record NormativeIndicatorEvidenceUploadResult(
        UUID evidenceId,
        int version,
        String contentHash,
        String event,
        IndicatorState indicatorState
) {
}
