package com.umss.sigesa.domain.model;

import java.util.List;
import java.util.UUID;

public record UpsertIndicatorAnalysisCommand(
        UUID indicatorId,
        String fodaQuadrant,
        String compliance,
        String synthesis,
        UUID authorId,
        List<String> roles,
        List<UUID> jwtProgramScope
) {
}
