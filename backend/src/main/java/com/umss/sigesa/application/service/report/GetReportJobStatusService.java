package com.umss.sigesa.application.service.report;

import com.umss.sigesa.application.port.in.GetReportJobStatusUseCase;
import com.umss.sigesa.application.port.out.ReportJobRepositoryPort;
import com.umss.sigesa.domain.exception.ReportAccessDeniedException;
import com.umss.sigesa.domain.exception.ReportJobNotFoundException;
import com.umss.sigesa.domain.model.ReportJob;
import com.umss.sigesa.domain.model.ReportJobStatus;

import java.util.UUID;

public class GetReportJobStatusService implements GetReportJobStatusUseCase {

    private final ReportJobRepositoryPort reportJobRepository;

    public GetReportJobStatusService(ReportJobRepositoryPort reportJobRepository) {
        this.reportJobRepository = reportJobRepository;
    }

    @Override
    public JobStatus getStatus(UUID jobId, UUID requesterId) {
        ReportJob job = reportJobRepository.findById(jobId)
                .orElseThrow(() -> new ReportJobNotFoundException(jobId));
        assertRequester(job, requesterId);

        String downloadPath = job.getStatus() == ReportJobStatus.COMPLETED
                ? "/api/v1/reports/executive/pdf/" + jobId + "/download"
                : null;

        return new JobStatus(job.getId(), job.getStatus(), downloadPath, job.getErrorCode());
    }

    static void assertRequester(ReportJob job, UUID requesterId) {
        if (!job.getRequesterId().equals(requesterId)) {
            throw new ReportAccessDeniedException();
        }
    }
}
