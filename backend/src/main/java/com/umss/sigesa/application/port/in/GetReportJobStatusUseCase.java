package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ReportExportJob;

import java.io.InputStream;
import java.util.UUID;

public interface GetReportJobStatusUseCase {
    ReportExportJob getJobStatus(UUID jobId, UUID requestingUserId);
    InputStream getJobFileStream(UUID jobId, UUID requestingUserId);
}
