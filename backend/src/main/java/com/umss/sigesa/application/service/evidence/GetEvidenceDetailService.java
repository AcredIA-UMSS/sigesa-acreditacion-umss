package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.evidence.EvidenceControlItem;
import com.umss.sigesa.application.port.in.GetEvidenceDetailUseCase;
import com.umss.sigesa.application.port.out.EvidenceControlQueryPort;

import java.util.Optional;
import java.util.UUID;

public class GetEvidenceDetailService implements GetEvidenceDetailUseCase {

    private final EvidenceControlQueryPort evidenceControlQueryPort;

    public GetEvidenceDetailService(EvidenceControlQueryPort evidenceControlQueryPort) {
        this.evidenceControlQueryPort = evidenceControlQueryPort;
    }

    @Override
    public Optional<EvidenceControlItem> get(AssistantAuthContext auth, UUID indicatorId) {
        if (indicatorId == null) {
            return Optional.empty();
        }
        Optional<EvidenceControlItem> item = evidenceControlQueryPort.findByIndicatorId(indicatorId);
        item.ifPresent(value -> EvidenceControlScopeSupport.assertIndicatorInScope(auth, value.programId()));
        return item;
    }
}
