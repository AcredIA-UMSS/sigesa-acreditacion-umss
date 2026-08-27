package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.EvidenceSearchCriteria;
import com.umss.sigesa.domain.model.EvidenceSearchPage;

import java.util.List;
import java.util.UUID;

public interface EvidenceSearchQueryPort {

    EvidenceSearchPage search(EvidenceSearchCriteria criteria, List<UUID> allowedProgramIds);
}
