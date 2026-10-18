package com.umss.sigesa.adapter.out.evidence;

import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "sigesa.evidence.storage", havingValue = "local", matchIfMissing = true)
public class LocalFileEvidenceBlobStorageAdapter implements EvidenceBlobStoragePort {

    private final Path storageRoot;

    public LocalFileEvidenceBlobStorageAdapter(
            @Value("${sigesa.evidence.storage-path:./data/evidences}") String storagePath) throws IOException {
        this.storageRoot = Path.of(storagePath);
        Files.createDirectories(storageRoot);
    }

    @Override
    public String store(UUID evidenceId, int version, byte[] content, String originalFilename) {
        String safeName = originalFilename == null ? "blob" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String key = evidenceId + "_v" + version + "_" + safeName;
        Path target = storageRoot.resolve(key);
        try {
            Files.write(target, content);
            return key;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store evidence blob", ex);
        }
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null) {
            return;
        }
        try {
            Files.deleteIfExists(storageRoot.resolve(storageKey));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to delete evidence blob: " + storageKey, ex);
        }
    }

    @Override
    public Optional<byte[]> retrieve(String storageKey) {
        if (storageKey == null || storageKey.isBlank() || storageKey.contains("..")) {
            return Optional.empty();
        }
        Path target = storageRoot.resolve(storageKey).normalize();
        if (!target.startsWith(storageRoot.normalize()) || !Files.exists(target)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(target));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read evidence blob: " + storageKey, ex);
        }
    }
}
