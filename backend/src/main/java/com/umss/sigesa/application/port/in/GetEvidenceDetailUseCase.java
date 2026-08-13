package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.evidence.EvidenceControlItem;

import java.util.Optional;
import java.util.UUID;

public interface GetEvidenceDetailUseCase {

    Optional<EvidenceControlItem> get(AssistantAuthContext auth, UUID indicatorId);
}
