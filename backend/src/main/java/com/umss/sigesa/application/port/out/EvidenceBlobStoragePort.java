package com.umss.sigesa.application.port.out;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
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

    /**
     * Abre un flujo de lectura directo hacia el blob sin cargarlo en memoria heap.
     * Retorna Optional.empty() si el objeto no existe en el storage.
     */
    default Optional<InputStream> openInputStream(String storageKey) {
        return retrieve(storageKey).map(ByteArrayInputStream::new);
    }

    /**
     * Crea un OutputStream para escribir directamente hacia el storage sin almacenar
     * el archivo completo en heap ni en disco efímero temporal.
     */
    OutputStream createOutputStream(String storageKey, String contentType);

    /**
     * Genera una URL prefirmada de descarga con expiración delimitada (S3/MinIO).
     * Retorna Optional.empty() si el backend usa almacenamiento local sin soporte de URL prefirmada.
     */
    default Optional<String> generatePresignedDownloadUrl(String storageKey, Duration duration) {
        return Optional.empty();
    }

    /**
     * Purga exportaciones de dossier expiradas (bajo prefijo dossier-exports/) que superen maxAge.
     */
    default void purgeExpiredDossierExports(Duration maxAge) {}
}
