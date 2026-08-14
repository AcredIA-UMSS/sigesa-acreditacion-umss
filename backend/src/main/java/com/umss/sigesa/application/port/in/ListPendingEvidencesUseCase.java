package com.umss.sigesa.application.port.in;

import com.umss.sigesa.application.model.assistant.AssistantAuthContext;
import com.umss.sigesa.application.model.evidence.EvidenceControlItem;

import java.util.List;
import java.util.UUID;

public interface ListPendingEvidencesUseCase {

    List<EvidenceControlItem> list(AssistantAuthContext auth, UUID programId);
}
