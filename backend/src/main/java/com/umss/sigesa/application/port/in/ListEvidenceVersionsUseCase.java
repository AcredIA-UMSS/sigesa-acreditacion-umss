package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.EvidenceVersionHistoryItem;

import java.util.List;
import java.util.UUID;

public interface ListEvidenceVersionsUseCase {

    List<EvidenceVersionHistoryItem> list(UUID evidenceId, UUID userId, List<String> roles);
}
