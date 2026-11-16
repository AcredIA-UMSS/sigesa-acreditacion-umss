package com.umss.sigesa.adapter.out.evidence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LocalFileEvidenceBlobStorageAdapter — Tests")
class LocalFileEvidenceBlobStorageAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("store, retrieve y openInputStream operan correctamente sobre almacenamiento local")
    void storeRetrieveAndOpenInputStream() throws Exception {
        LocalFileEvidenceBlobStorageAdapter adapter =
                new LocalFileEvidenceBlobStorageAdapter(tempDir.toString());

        UUID evidenceId = UUID.randomUUID();
        String originalFilename = "informe final.pdf";
        byte[] content = "PDF_CONTENT_SAMPLE".getBytes();

        String storageKey = adapter.store(evidenceId, 1, content, originalFilename);
        assertNotNull(storageKey);
        assertTrue(storageKey.startsWith("evidences/" + evidenceId + "/v1/"));

        Optional<byte[]> retrievedBytes = adapter.retrieve(storageKey);
        assertTrue(retrievedBytes.isPresent());
        assertArrayEquals(content, retrievedBytes.get());

        Optional<InputStream> streamOpt = adapter.openInputStream(storageKey);
        assertTrue(streamOpt.isPresent());
        try (InputStream is = streamOpt.get()) {
            byte[] streamBytes = is.readAllBytes();
            assertArrayEquals(content, streamBytes);
        }
    }

    @Test
    @DisplayName("createOutputStream escribe y permite streaming con openInputStream")
    void createOutputStreamAndOpenInputStream() throws Exception {
        LocalFileEvidenceBlobStorageAdapter adapter =
                new LocalFileEvidenceBlobStorageAdapter(tempDir.toString());

        String key = "dossier-exports/test-job/dossier.zip";
        byte[] zipBytes = "PK\003\004ZIP_DUMMY_STREAM".getBytes();

        try (OutputStream os = adapter.createOutputStream(key, "application/zip")) {
            os.write(zipBytes);
            os.flush();
        }

        Optional<InputStream> streamOpt = adapter.openInputStream(key);
        assertTrue(streamOpt.isPresent());
        try (InputStream is = streamOpt.get()) {
            assertArrayEquals(zipBytes, is.readAllBytes());
        }
    }

    @Test
    @DisplayName("openInputStream retorna Optional.empty() cuando la clave no existe o es inválida")
    void openInputStreamMissingKey() throws Exception {
        LocalFileEvidenceBlobStorageAdapter adapter =
                new LocalFileEvidenceBlobStorageAdapter(tempDir.toString());

        assertTrue(adapter.openInputStream("non-existent-key.pdf").isEmpty());
        assertTrue(adapter.openInputStream(null).isEmpty());
        assertTrue(adapter.openInputStream("../path-traversal.pdf").isEmpty());
    }
}
