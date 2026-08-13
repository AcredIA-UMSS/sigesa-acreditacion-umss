package com.umss.sigesa.application.port.in;

import java.util.List;
import java.util.UUID;

public interface DownloadEvidenceUseCase {
    EvidenceFileResult download(UUID versionId, UUID userId, String role, List<UUID> programScope);

    record EvidenceFileResult(String filename, String contentType, byte[] content) {
    }
}
