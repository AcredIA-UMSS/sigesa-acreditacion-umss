package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ReportJobStatus;

import java.util.UUID;

public interface GetReportJobStatusUseCase {

    JobStatus getStatus(UUID jobId, UUID requesterId);

    record JobStatus(UUID jobId, ReportJobStatus status, String downloadPath, String errorCode) {
    }
}
