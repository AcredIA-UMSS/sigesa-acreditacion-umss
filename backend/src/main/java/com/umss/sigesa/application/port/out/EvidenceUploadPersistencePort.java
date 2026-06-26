package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import com.umss.sigesa.domain.model.IndicatorStateHistoryEntry;

/**
 * Persiste evidencia v1 y transición de estado en una única unidad transaccional JPA.
 */
public interface EvidenceUploadPersistencePort {

    void persistUpload(Evidence evidence, EvidenceVersion version, IndicatorStateHistoryEntry historyEntry);
}
