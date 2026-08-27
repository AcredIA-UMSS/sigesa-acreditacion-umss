package com.umss.sigesa.domain.model;

import java.util.UUID;

public record EvidenceSubsanationResult(
        UUID evidenceId,
        int version,
        UUID observationId,
        int supersedesVersion,
        String contentHash,
        String event) {
}
