package com.umss.sigesa.application.model.process;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessSummary(
        UUID id,
        UUID careerId,
        String careerCode,
        String careerName,
        UUID templateId,
        String templateName,
        String templateType,
        String status,
        LocalDateTime startDate,
        int phaseCount,
        int subphaseCount
) {
}
