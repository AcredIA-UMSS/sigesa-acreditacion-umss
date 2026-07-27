package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ReportExportJob;

import java.io.InputStream;
import java.util.UUID;

/**
 * Estado y descarga de jobs de exportación del dashboard (UC-011).
 */
public interface GetExportReportJobStatusUseCase {

    ReportExportJob getJobStatus(UUID jobId, UUID requestingUserId);

    InputStream getJobFileStream(UUID jobId, UUID requestingUserId);
}
