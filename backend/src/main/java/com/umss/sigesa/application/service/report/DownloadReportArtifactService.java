package com.umss.sigesa.application.service.report;

import com.umss.sigesa.application.port.in.DownloadReportArtifactUseCase;
import com.umss.sigesa.application.port.out.ReportArtifactStoragePort;
import com.umss.sigesa.application.port.out.ReportJobRepositoryPort;
import com.umss.sigesa.domain.exception.ReportJobNotFoundException;
import com.umss.sigesa.domain.exception.ReportNotReadyException;
import com.umss.sigesa.domain.model.ReportJob;
import com.umss.sigesa.domain.model.ReportJobStatus;

import java.util.UUID;

public class DownloadReportArtifactService implements DownloadReportArtifactUseCase {

    private final ReportJobRepositoryPort reportJobRepository;
    private final ReportArtifactStoragePort artifactStorage;

    public DownloadReportArtifactService(ReportJobRepositoryPort reportJobRepository,
                                         ReportArtifactStoragePort artifactStorage) {
        this.reportJobRepository = reportJobRepository;
        this.artifactStorage = artifactStorage;
    }

    @Override
    public Artifact download(UUID jobId, UUID requesterId) {
        ReportJob job = reportJobRepository.findById(jobId)
                .orElseThrow(() -> new ReportJobNotFoundException(jobId));
        GetReportJobStatusService.assertRequester(job, requesterId);

        if (job.getStatus() != ReportJobStatus.COMPLETED || job.getArtifactKey() == null) {
            throw new ReportNotReadyException();
        }

        byte[] content = artifactStorage.retrieve(job.getArtifactKey())
                .orElseThrow(() -> new ReportJobNotFoundException(jobId));

        return new Artifact(content, "sigesa-reporte-ejecutivo-" + jobId + ".pdf");
    }
}
