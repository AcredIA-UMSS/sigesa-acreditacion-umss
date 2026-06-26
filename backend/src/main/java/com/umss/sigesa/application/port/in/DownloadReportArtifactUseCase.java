package com.umss.sigesa.application.port.in;

import java.util.UUID;

public interface DownloadReportArtifactUseCase {

    Artifact download(UUID jobId, UUID requesterId);

    record Artifact(byte[] content, String filename) {
    }
}
