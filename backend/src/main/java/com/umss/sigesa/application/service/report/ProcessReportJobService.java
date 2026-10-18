package com.umss.sigesa.application.service.report;

import com.umss.sigesa.application.port.in.ProcessReportJobUseCase;
import com.umss.sigesa.application.port.out.ExecutiveDataPort;
import com.umss.sigesa.application.port.out.PdfRendererPort;
import com.umss.sigesa.application.port.out.ReportArtifactStoragePort;
import com.umss.sigesa.application.port.out.ReportJobRepositoryPort;
import com.umss.sigesa.application.port.out.TransactionRunnerPort;
import com.umss.sigesa.domain.exception.ReportJobNotFoundException;
import com.umss.sigesa.domain.exception.ReportTemplateException;
import com.umss.sigesa.domain.model.ExecutiveReportSnapshot;
import com.umss.sigesa.domain.model.ReportJob;

import java.util.UUID;

public class ProcessReportJobService implements ProcessReportJobUseCase {

    public static final String ERROR_REPORT_TEMPLATE = "REPORT_TEMPLATE";
    public static final String ERROR_GENERATION_FAILED = "REPORT_GENERATION_FAILED";

    private final ReportJobRepositoryPort reportJobRepository;
    private final ExecutiveDataPort executiveDataPort;
    private final PdfRendererPort pdfRendererPort;
    private final ReportArtifactStoragePort artifactStorage;
    private final TransactionRunnerPort transactions;

    public ProcessReportJobService(ReportJobRepositoryPort reportJobRepository,
                                   ExecutiveDataPort executiveDataPort,
                                   PdfRendererPort pdfRendererPort,
                                   ReportArtifactStoragePort artifactStorage,
                                   TransactionRunnerPort transactions) {
        this.reportJobRepository = reportJobRepository;
        this.executiveDataPort = executiveDataPort;
        this.pdfRendererPort = pdfRendererPort;
        this.artifactStorage = artifactStorage;
        this.transactions = transactions;
    }

    @Override
    public void process(UUID jobId) {
        ReportJob job = transactions.required(() -> {
            ReportJob found = reportJobRepository.findById(jobId)
                    .orElseThrow(() -> new ReportJobNotFoundException(jobId));
            found.markInProgress();
            return reportJobRepository.save(found);
        });

        try {
            ExecutiveReportSnapshot snapshot = transactions.readOnly(
                    () -> executiveDataPort.fetchSnapshot(job.getFilters()));
            byte[] pdfBytes = pdfRendererPort.render(snapshot);
            String artifactKey = artifactStorage.store(jobId, pdfBytes);
            transactions.required(() -> {
                job.markCompleted(artifactKey);
                reportJobRepository.save(job);
            });
        } catch (ReportTemplateException ex) {
            transactions.required(() -> {
                job.markFailed(ERROR_REPORT_TEMPLATE);
                reportJobRepository.save(job);
            });
        } catch (RuntimeException ex) {
            transactions.required(() -> {
                job.markFailed(ERROR_GENERATION_FAILED);
                reportJobRepository.save(job);
            });
        }
    }
}
