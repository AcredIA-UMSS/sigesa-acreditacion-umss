package com.umss.sigesa.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface EvidenceBlobStoragePort {

    /**
     * Persiste el blob y retorna una clave opaca (nunca una ruta absoluta de disco).
     * Compatible con filesystem local o object storage S3/MinIO.
     */
    String store(UUID evidenceId, int version, byte[] content, String originalFilename);

    /**
     * Reescribe el objeto en una clave ya conocida (p. ej. reutilizar {@code DocumentAsset}
     * cuyo blob fue purgado por error).
     */
    void restore(String storageKey, byte[] content);

    void delete(String storageKey);

    Optional<byte[]> retrieve(String storageKey);
}
