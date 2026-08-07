package com.umss.sigesa.application.model.process;

import com.umss.sigesa.domain.model.Phase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EnrichedProcessDetail(
        UUID id,
        UUID careerId,
        String careerCode,
        String careerName,
        UUID templateId,
        String templateName,
        String templateType,
        String status,
        LocalDateTime startDate,
        List<Phase> phases,
        ProcessResponsibleInfo responsible
) {
}
