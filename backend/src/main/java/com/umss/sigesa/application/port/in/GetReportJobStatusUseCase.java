package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ReportJobStatus;

import java.util.UUID;

/**
 * Estado de job del reporte ejecutivo PDF (UC-014 / MOD-REPORT).
 */
public interface GetReportJobStatusUseCase {

    JobStatus getStatus(UUID jobId, UUID requesterId);

    record JobStatus(UUID jobId, ReportJobStatus status, String downloadPath, String errorCode) {
    }
}
