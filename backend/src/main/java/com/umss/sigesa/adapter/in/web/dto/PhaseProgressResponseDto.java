package com.umss.sigesa.adapter.in.web.dto;

import com.umss.sigesa.domain.model.PhaseState;

import java.util.UUID;

public record PhaseProgressResponseDto(
        UUID phaseId,
        int totalSubphases,
        int approvedSubphases,
        int pendingSubphases,
        double progressPercent,
        PhaseState state
) {
}
