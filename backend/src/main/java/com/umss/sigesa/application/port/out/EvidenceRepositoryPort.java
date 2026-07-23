package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import java.util.UUID;

public interface EvidenceRepositoryPort {
    void save(Evidence evidence, EvidenceVersion version);
    UUID findProgramIdForIndicator(UUID indicatorId);
}
