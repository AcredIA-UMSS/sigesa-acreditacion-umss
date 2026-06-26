package com.umss.sigesa.application.service.report;

import com.umss.sigesa.application.port.in.ProcessReportJobUseCase;
import com.umss.sigesa.application.port.out.ExecutiveDataPort;
import com.umss.sigesa.application.port.out.PdfRendererPort;
import com.umss.sigesa.application.port.out.ReportArtifactStoragePort;
import com.umss.sigesa.application.port.out.ReportJobRepositoryPort;
import com.umss.sigesa.domain.exception.ReportJobNotFoundException;
import com.umss.sigesa.domain.exception.ReportTemplateException;
import com.umss.sigesa.domain.model.ReportJob;

import java.util.UUID;

public class ProcessReportJobService implements ProcessReportJobUseCase {

    public static final String ERROR_REPORT_TEMPLATE = "REPORT_TEMPLATE";
    public static final String ERROR_GENERATION_FAILED = "REPORT_GENERATION_FAILED";

    private final ReportJobRepositoryPort reportJobRepository;
    private final ExecutiveDataPort executiveDataPort;
    private final PdfRendererPort pdfRendererPort;
    private final ReportArtifactStoragePort artifactStorage;

    public ProcessReportJobService(ReportJobRepositoryPort reportJobRepository,
                                   ExecutiveDataPort executiveDataPort,
                                   PdfRendererPort pdfRendererPort,
                                   ReportArtifactStoragePort artifactStorage) {
        this.reportJobRepository = reportJobRepository;
        this.executiveDataPort = executiveDataPort;
        this.pdfRendererPort = pdfRendererPort;
        this.artifactStorage = artifactStorage;
    }

    @Override
    public void process(UUID jobId) {
        ReportJob job = reportJobRepository.findById(jobId)
                .orElseThrow(() -> new ReportJobNotFoundException(jobId));

        job.markInProgress();
        reportJobRepository.save(job);

        try {
            var snapshot = executiveDataPort.fetchSnapshot(job.getFilters());
            byte[] pdfBytes = pdfRendererPort.render(snapshot);
            String artifactKey = artifactStorage.store(jobId, pdfBytes);
            job.markCompleted(artifactKey);
            reportJobRepository.save(job);
        } catch (ReportTemplateException ex) {
            job.markFailed(ERROR_REPORT_TEMPLATE);
            reportJobRepository.save(job);
        } catch (RuntimeException ex) {
            job.markFailed(ERROR_GENERATION_FAILED);
            reportJobRepository.save(job);
        }
    }
}
