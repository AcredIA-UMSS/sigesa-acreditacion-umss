package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class DocumentAssetNotFoundException extends RuntimeException {

    public DocumentAssetNotFoundException(UUID documentAssetId) {
        super("DocumentAsset no encontrado: " + documentAssetId);
    }
}
