package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class DocumentAssetNotFoundException extends RuntimeException {

    private final UUID documentAssetId;

    public DocumentAssetNotFoundException(UUID documentAssetId) {
        super("El documento solicitado no fue encontrado.");
        this.documentAssetId = documentAssetId;
    }

    public DocumentAssetNotFoundException(String message) {
        super(message);
        this.documentAssetId = null;
    }

    public UUID getDocumentAssetId() {
        return documentAssetId;
    }
}
