package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.domain.model.IndicatorState;

import java.util.UUID;

public interface CheckEvidenceCompletenessUseCase {

    record CompletenessChecklist(
            UUID indicatorId,
            boolean hasEvidence,
            boolean hasDescription,
            boolean hasCriterion,
            boolean hasContentHash,
            IndicatorState currentState,
            boolean complete
    ) {
    }

    CompletenessChecklist check(AssistantAuthContext auth, UUID indicatorId);
}
