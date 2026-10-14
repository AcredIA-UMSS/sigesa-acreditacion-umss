package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import com.umss.sigesa.domain.model.IndicatorEvidenceLink;
import com.umss.sigesa.domain.model.IndicatorStateHistoryEntry;

import java.util.UUID;

/**
 * Persiste evidencia y transición de estado en una única unidad transaccional JPA.
 */
public interface EvidenceUploadPersistencePort {

    void persistUpload(Evidence evidence, EvidenceVersion version, IndicatorStateHistoryEntry historyEntry);

    void persistNormativeIndicatorUpload(Evidence evidence, EvidenceVersion version, String externalUrl);

    void persistNormativeIndicatorUpload(
            Evidence evidence,
            EvidenceVersion version,
            String externalUrl,
            DocumentAsset documentAsset,
            boolean persistNewAsset,
            IndicatorEvidenceLink link);

    String persistNormativeIndicatorSubsanation(
            UUID evidenceId,
            EvidenceVersion newVersion,
            UUID observationId,
            int supersedesVersionNumber,
            UUID supersededVersionId);
}
