package com.umss.sigesa.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IndicatorCatalogPort {

    List<IndicatorEntry> findAll(UUID programId, Integer phaseId);

    Optional<IndicatorEntry> findById(UUID indicatorId);

    record IndicatorEntry(
            UUID id,
            String code,
            String title,
            UUID programId,
            int phaseId,
            UUID criterionId
    ) {
    }
}
