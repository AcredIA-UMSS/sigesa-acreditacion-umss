package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.port.out.IndicatorCatalogPort.IndicatorEntry;

import java.util.List;
import java.util.UUID;

public interface ListIndicatorsUseCase {

    List<IndicatorSummary> list(UUID programId, Integer phaseId, List<UUID> allowedProgramIds);

    record IndicatorSummary(
            UUID id,
            String code,
            String title,
            UUID programId,
            int phaseId,
            UUID criterionId,
            String currentState
    ) {
        static IndicatorSummary from(IndicatorEntry entry, String currentState) {
            return new IndicatorSummary(
                    entry.id(),
                    entry.code(),
                    entry.title(),
                    entry.programId(),
                    entry.phaseId(),
                    entry.criterionId(),
                    currentState
            );
        }
    }
}
