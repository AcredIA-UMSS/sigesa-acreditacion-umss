package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.IndicatorEvidenceLink;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentAssetPort {

    Optional<DocumentAsset> findById(UUID id);

    Optional<DocumentAsset> findByHashInProcess(UUID processId, String contentHash);

    List<DocumentAsset> findByProcessId(UUID processId);

    Optional<DocumentAsset> findByEvidenceId(UUID evidenceId);

    DocumentAsset save(DocumentAsset asset);

    IndicatorEvidenceLink saveLink(IndicatorEvidenceLink link);

    List<UUID> findLinkedIndicatorIds(UUID documentAssetId);
}
