package com.umss.sigesa.application.model.evidence;

import com.umss.sigesa.domain.model.IndicatorState;

import java.time.LocalDateTime;
import java.util.UUID;

public record EvidenceControlItem(
        UUID indicatorId,
        UUID programId,
        UUID criterionId,
        UUID phaseId,
        IndicatorState currentState,
        UUID evidenceId,
        Integer versionNumber,
        String contentHash,
        String description,
        LocalDateTime createdAt
) {
}
