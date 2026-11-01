package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.DocumentAssetView;

import java.util.List;
import java.util.UUID;

public interface GetDocumentAssetUseCase {

    DocumentAssetView get(UUID documentAssetId, UUID requesterId, List<String> roles);

    List<DocumentAssetView> listByProcess(UUID processId, UUID requesterId, List<String> roles);

    DocumentAssetBinary download(UUID documentAssetId, UUID requesterId, List<String> roles);

    record DocumentAssetBinary(byte[] content, String filename, String mediaType) {}
}
