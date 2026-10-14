package com.umss.sigesa.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateImprovementPlanItemCommand(
        UUID processId,
        UUID itemId,
        String objective,
        String activity,
        UUID responsibleId,
        LocalDate deadline,
        Integer progress,
        String justification,
        UUID progressEvidenceAssetId,
        UUID requesterId,
        List<String> roles,
        List<UUID> jwtProgramScope
) {
}
