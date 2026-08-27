package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.EvidenceSearchCriteria;
import com.umss.sigesa.domain.model.EvidenceSearchPage;

import java.util.List;
import java.util.UUID;

public interface SearchEvidencesUseCase {

    EvidenceSearchPage search(EvidenceSearchCriteria criteria, UUID requesterId, List<String> roles);
}
