package com.umss.sigesa.application.port.out;

import java.util.UUID;

public interface EvidenceBlobStoragePort {

    String store(UUID evidenceId, int version, byte[] content, String originalFilename);

    void delete(String storageKey);
}
