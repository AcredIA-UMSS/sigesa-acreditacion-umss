package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.evidence.EvidenceControlItem;
import com.umss.sigesa.application.port.in.ListPendingEvidencesUseCase;
import com.umss.sigesa.application.port.out.EvidenceControlQueryPort;
import com.umss.sigesa.domain.model.IndicatorState;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ListPendingEvidencesService implements ListPendingEvidencesUseCase {

    private static final Set<IndicatorState> PENDING_STATES = Set.of(IndicatorState.SUBIDO);

    private final EvidenceControlQueryPort evidenceControlQueryPort;

    public ListPendingEvidencesService(EvidenceControlQueryPort evidenceControlQueryPort) {
        this.evidenceControlQueryPort = evidenceControlQueryPort;
    }

    @Override
    public List<EvidenceControlItem> list(AssistantAuthContext auth, UUID programId) {
        List<UUID> programFilter = EvidenceControlScopeSupport.resolveProgramFilter(auth, programId);
        return evidenceControlQueryPort.listByProgramIdsAndStates(programFilter, PENDING_STATES);
    }
}
