package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ProcessType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ListTemplatesUseCase {

    List<TemplateSummary> list();

    record TemplateSummary(
            UUID id,
            boolean validated,
            String taxonomyVersion,
            String activePeriod,
            LocalDateTime activatedAt,
            ProcessType type
    ) {
    }
}
