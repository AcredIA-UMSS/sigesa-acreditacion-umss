package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.in.ListIndicatorsUseCase;
import com.umss.sigesa.application.port.out.IndicatorCatalogPort;
import com.umss.sigesa.application.port.out.IndicatorStateHistoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListIndicatorsService implements ListIndicatorsUseCase {

    private final IndicatorCatalogPort indicatorCatalog;
    private final IndicatorStateHistoryPort indicatorStateHistory;

    public ListIndicatorsService(IndicatorCatalogPort indicatorCatalog,
                                 IndicatorStateHistoryPort indicatorStateHistory) {
        this.indicatorCatalog = indicatorCatalog;
        this.indicatorStateHistory = indicatorStateHistory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IndicatorSummary> list(UUID programId, Integer phaseId, List<UUID> allowedProgramIds) {
        return indicatorCatalog.findAll(programId, phaseId).stream()
                .filter(entry -> allowedProgramIds == null
                        || allowedProgramIds.isEmpty()
                        || allowedProgramIds.contains(entry.programId()))
                .map(entry -> IndicatorSummary.from(
                        entry,
                        indicatorStateHistory.findLatestState(entry.id()).orElse("PENDIENTE")
                ))
                .toList();
    }
}
