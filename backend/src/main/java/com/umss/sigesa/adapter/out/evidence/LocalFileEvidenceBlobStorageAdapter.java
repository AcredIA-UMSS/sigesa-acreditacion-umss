package com.umss.sigesa.adapter.out.evidence;

import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@ConditionalOnProperty(name = "sigesa.evidence.storage", havingValue = "local", matchIfMissing = true)
public class LocalFileEvidenceBlobStorageAdapter implements EvidenceBlobStoragePort {

    private static final Logger log = LoggerFactory.getLogger(LocalFileEvidenceBlobStorageAdapter.class);

    private final Path storageRoot;

    public LocalFileEvidenceBlobStorageAdapter(
            @Value("${sigesa.evidence.storage-path:./data}") String storagePath) throws IOException {
        this.storageRoot = Path.of(storagePath);
        Files.createDirectories(storageRoot);
    }

    @Override
    public String store(UUID evidenceId, int version, byte[] content, String originalFilename) {
        String safeName = originalFilename == null ? "blob" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        // Same key format as S3 adapter: evidences/{evidenceId}/v{version}/{filename}
        String key = "evidences/" + evidenceId + "/v" + version + "/" + safeName;
        Path target = storageRoot.resolve(key).normalize();
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return key;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store evidence blob", ex);
        }
    }

    @Override
    public void restore(String storageKey, byte[] content) {
        if (storageKey == null || storageKey.isBlank() || storageKey.contains("..")) {
            throw new IllegalArgumentException("Invalid storageKey");
        }
        Path target = storageRoot.resolve(storageKey).normalize();
        if (!target.startsWith(storageRoot.normalize())) {
            throw new IllegalArgumentException("Invalid storageKey");
        }
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(target, content == null ? new byte[0] : content);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to restore evidence blob", ex);
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

    @Override
    public Optional<InputStream> openInputStream(String storageKey) {
        if (storageKey == null || storageKey.isBlank() || storageKey.contains("..")) {
            return Optional.empty();
        }
        Path target = storageRoot.resolve(storageKey).normalize();
        if (!target.startsWith(storageRoot.normalize()) || !Files.exists(target)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.newInputStream(target));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to open stream for evidence blob: " + storageKey, ex);
        }
    }

    @Override
    public OutputStream createOutputStream(String storageKey, String contentType) {
        if (storageKey == null || storageKey.isBlank() || storageKey.contains("..")) {
            throw new IllegalArgumentException("Invalid storageKey");
        }
        Path target = storageRoot.resolve(storageKey).normalize();
        if (!target.startsWith(storageRoot.normalize())) {
            throw new IllegalArgumentException("Invalid storageKey");
        }
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            return Files.newOutputStream(target);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create output stream for " + storageKey, ex);
        }
    }

    @Override
    public void purgeExpiredDossierExports(Duration maxAge) {
        try {
            Path exportsDir = storageRoot.resolve("dossier-exports").normalize();
            if (Files.exists(exportsDir)) {
                Instant threshold = Instant.now().minus(maxAge);
                try (Stream<Path> stream = Files.walk(exportsDir)) {
                    stream.filter(Files::isRegularFile)
                            .filter(p -> {
                                try {
                                    return Files.getLastModifiedTime(p).toInstant().isBefore(threshold);
                                } catch (IOException e) {
                                    return false;
                                }
                            })
                            .forEach(p -> {
                                try {
                                    Files.deleteIfExists(p);
                                } catch (IOException ignored) {}
                            });
                }
            }
            // Cleanup any orphan legacy temp files
            String tmpDir = System.getProperty("java.io.tmpdir", "/tmp");
            Path tmpPath = Path.of(tmpDir);
            if (Files.exists(tmpPath)) {
                Instant threshold = Instant.now().minus(maxAge);
                try (Stream<Path> stream = Files.list(tmpPath)) {
                    stream.filter(p -> p.getFileName().toString().startsWith("dossier-") && p.getFileName().toString().endsWith(".zip"))
                            .filter(p -> {
                                try {
                                    return Files.getLastModifiedTime(p).toInstant().isBefore(threshold);
                                } catch (IOException e) {
                                    return false;
                                }
                            })
                            .forEach(p -> {
                                try {
                                    Files.deleteIfExists(p);
                                } catch (IOException ignored) {}
                            });
                } catch (Exception ignored) {}
            }
        } catch (Exception ex) {
            log.warn("Error purging expired dossier exports in local storage: {}", ex.getMessage());
        }
    }
}
