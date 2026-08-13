package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.evidence.EvidenceControlItem;
import com.umss.sigesa.application.port.in.CheckEvidenceCompletenessUseCase;
import com.umss.sigesa.application.port.out.EvidenceControlQueryPort;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.model.IndicatorState;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class CheckEvidenceCompletenessService implements CheckEvidenceCompletenessUseCase {

    private static final Set<IndicatorState> COMPLETE_STATES = EnumSet.of(
            IndicatorState.SUBIDO,
            IndicatorState.SUBSANADO,
            IndicatorState.APROBADO);

    private final EvidenceControlQueryPort evidenceControlQueryPort;

    public CheckEvidenceCompletenessService(EvidenceControlQueryPort evidenceControlQueryPort) {
        this.evidenceControlQueryPort = evidenceControlQueryPort;
    }

    @Override
    public CompletenessChecklist check(AssistantAuthContext auth, UUID indicatorId) {
        if (indicatorId == null) {
            throw new IndicatorNotFoundException(null);
        }
        EvidenceControlItem item = evidenceControlQueryPort.findByIndicatorId(indicatorId)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));
        EvidenceControlScopeSupport.assertIndicatorInScope(auth, item.programId());

        boolean hasEvidence = item.evidenceId() != null;
        boolean hasDescription = item.description() != null && !item.description().isBlank();
        boolean hasCriterion = item.criterionId() != null;
        boolean hasContentHash = item.contentHash() != null && !item.contentHash().isBlank();
        IndicatorState currentState = item.currentState();
        boolean complete = hasEvidence
                && hasDescription
                && hasCriterion
                && hasContentHash
                && currentState != null
                && COMPLETE_STATES.contains(currentState);

        return new CompletenessChecklist(
                item.indicatorId(),
                hasEvidence,
                hasDescription,
                hasCriterion,
                hasContentHash,
                currentState,
                complete);
    }
}