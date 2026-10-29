package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ReportExportJob;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

public interface GetProcessDossierExportJobStatusUseCase {

    ReportExportJob getJobStatus(UUID jobId, UUID requestingUserId);

    InputStream getJobFileStream(UUID jobId, UUID requestingUserId);

    default Optional<String> getPresignedDownloadUrl(UUID jobId, UUID requestingUserId) {
        return Optional.empty();
    }
}
