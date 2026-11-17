package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class EvidenceBlobNotFoundException extends RuntimeException {

    private final UUID documentAssetId;

    public EvidenceBlobNotFoundException(UUID documentAssetId) {
        super("El archivo de la evidencia no se encuentra disponible en el almacenamiento.");
        this.documentAssetId = documentAssetId;
    }

    public EvidenceBlobNotFoundException(String message) {
        super(message);
        this.documentAssetId = null;
    }

    public UUID getDocumentAssetId() {
        return documentAssetId;
    }
}
