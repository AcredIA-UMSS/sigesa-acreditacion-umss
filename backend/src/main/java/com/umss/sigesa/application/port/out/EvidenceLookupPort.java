package com.umss.sigesa.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvidenceLookupPort {

    record EvidenceRef(
            UUID evidenceId,
            UUID indicatorId,
            UUID programId,
            String code,
            String title,
            String originalFilename,
            UUID documentAssetId,
            String folioCode
    ) {
        public EvidenceRef(UUID evidenceId, UUID indicatorId, UUID programId, String code, String title) {
            this(evidenceId, indicatorId, programId, code, title, null, null, null);
        }
    }

    Optional<EvidenceRef> findById(UUID evidenceId);

    List<EvidenceRef> listByProgramId(UUID programId);
}
