package com.umss.sigesa.adapter.in.web.dto;

import java.util.UUID;

public record UploadEvidenceResponse(
        UUID evidenceId,
        int version,
        String contentHash,
        String event,
        String currentState
) {
}
