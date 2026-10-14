package com.umss.sigesa.domain.model;

import java.util.UUID;

public record NormativeIndicatorEvidenceUploadResult(
        UUID evidenceId,
        int version,
        String contentHash,
        String event,
        IndicatorState indicatorState,
        UUID documentAssetId,
        String folioCode,
        boolean reusedAsset
) {
    public NormativeIndicatorEvidenceUploadResult(
            UUID evidenceId,
            int version,
            String contentHash,
            String event,
            IndicatorState indicatorState) {
        this(evidenceId, version, contentHash, event, indicatorState, null, null, false);
    }
}
