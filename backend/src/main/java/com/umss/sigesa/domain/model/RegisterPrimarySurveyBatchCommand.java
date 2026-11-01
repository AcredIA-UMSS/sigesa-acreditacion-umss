package com.umss.sigesa.domain.model;

import java.util.List;
import java.util.UUID;

public record RegisterPrimarySurveyBatchCommand(
        UUID processId,
        String audience,
        Integer responsesCount,
        UUID documentAssetId,
        UUID registeredBy,
        List<String> roles,
        List<UUID> jwtProgramScope
) {
}
