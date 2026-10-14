package com.umss.sigesa.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateImprovementPlanItemCommand(
        UUID processId,
        String objective,
        String activity,
        UUID responsibleId,
        LocalDate deadline,
        Integer progress,
        UUID requesterId,
        List<String> roles,
        List<UUID> jwtProgramScope
) {
}
