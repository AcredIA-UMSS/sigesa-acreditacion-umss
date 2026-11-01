package com.umss.sigesa.domain.exception;

import java.util.UUID;

public class EvidenceBlobNotFoundException extends RuntimeException {

    public EvidenceBlobNotFoundException(UUID documentAssetId) {
        super("Blob not found in object storage for document asset " + documentAssetId);
    }
}
