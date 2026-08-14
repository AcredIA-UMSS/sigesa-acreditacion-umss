package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.model.evidence.UploadableIndicator;
import com.umss.sigesa.application.port.in.ListUploadableIndicatorsUseCase;
import com.umss.sigesa.application.port.out.EvidenceControlQueryPort;
import com.umss.sigesa.domain.model.IndicatorState;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ListUploadableIndicatorsService implements ListUploadableIndicatorsUseCase {

    private static final Set<IndicatorState> UPLOADABLE_STATES =
            EnumSet.of(IndicatorState.PENDIENTE, IndicatorState.OBSERVADO);

    private final EvidenceControlQueryPort evidenceControlQueryPort;

    public ListUploadableIndicatorsService(EvidenceControlQueryPort evidenceControlQueryPort) {
        this.evidenceControlQueryPort = evidenceControlQueryPort;
    }

    @Override
    public List<UploadableIndicator> listForCoordinator(List<UUID> programScope) {
        if (programScope == null || programScope.isEmpty()) {
            return List.of();
        }
        return evidenceControlQueryPort.listUploadableByProgramIds(programScope, UPLOADABLE_STATES);
    }
}
