package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.SubphaseEvidenceItem;

import java.util.List;
import java.util.UUID;

public interface ListSubphaseEvidencesUseCase {

    List<SubphaseEvidenceItem> list(UUID subphaseId, UUID requesterId, List<String> roles);
}
