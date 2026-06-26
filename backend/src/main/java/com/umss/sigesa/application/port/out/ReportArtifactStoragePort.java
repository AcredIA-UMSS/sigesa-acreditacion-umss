package com.umss.sigesa.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface ReportArtifactStoragePort {

    String store(UUID jobId, byte[] pdfContent);

    Optional<byte[]> retrieve(String artifactKey);
}
