package com.umss.sigesa.adapter.out.report;

import com.umss.sigesa.application.port.out.ReportArtifactStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalFileReportArtifactStorageAdapter implements ReportArtifactStoragePort {

    private final Path storageRoot;

    public LocalFileReportArtifactStorageAdapter(
            @Value("${sigesa.report.storage-path:./data/reports}") String storagePath) throws IOException {
        this.storageRoot = Path.of(storagePath);
        Files.createDirectories(storageRoot);
    }

    @Override
    public String store(UUID jobId, byte[] pdfContent) {
        String key = jobId + ".pdf";
        Path target = storageRoot.resolve(key);
        try {
            Files.write(target, pdfContent);
            return key;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to store report artifact", ex);
        }
    }

    @Override
    public Optional<byte[]> retrieve(String artifactKey) {
        Path target = storageRoot.resolve(artifactKey);
        if (!Files.exists(target)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(target));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read report artifact", ex);
        }
    }
}
