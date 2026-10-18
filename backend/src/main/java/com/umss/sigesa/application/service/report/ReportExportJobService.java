package com.umss.sigesa.application.service.report;

import com.umss.sigesa.application.port.in.ExportReportJobUseCase;
import com.umss.sigesa.application.port.in.GetExportReportJobStatusUseCase;
import com.umss.sigesa.application.port.out.DashboardQueryPort;
import com.umss.sigesa.application.port.out.ReportExportJobRepositoryPort;
import com.umss.sigesa.application.port.out.ReportGeneratorPort;
import com.umss.sigesa.application.port.out.TransactionRunnerPort;
import com.umss.sigesa.domain.exception.InvalidJobStateException;
import com.umss.sigesa.domain.exception.JobNotFoundException;
import com.umss.sigesa.domain.model.JobStatus;
import com.umss.sigesa.domain.model.ObservationSummary;
import com.umss.sigesa.domain.model.ReportExportJob;
import com.umss.sigesa.domain.model.ReportFormat;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class ReportExportJobService implements ExportReportJobUseCase, GetExportReportJobStatusUseCase {

    private final ReportExportJobRepositoryPort repositoryPort;
    private final DashboardQueryPort queryPort;
    private final List<ReportGeneratorPort> reportGenerators;
    private final TransactionRunnerPort transactions;

    public ReportExportJobService(ReportExportJobRepositoryPort repositoryPort,
                                  DashboardQueryPort queryPort,
                                  List<ReportGeneratorPort> reportGenerators,
                                  TransactionRunnerPort transactions) {
        this.repositoryPort = repositoryPort;
        this.queryPort = queryPort;
        this.reportGenerators = reportGenerators;
        this.transactions = transactions;
    }

    @Override
    public ReportExportJob enqueueJob(UUID userId, UUID programId, ReportFormat format, Integer phaseId) {
        ReportExportJob job = ReportExportJob.createNew(userId, programId, format, phaseId);
        return transactions.required(() -> repositoryPort.save(job));
    }

    /**
     * CON-03 / FEAT-01: persistir estado en TX cortas; generar el archivo fuera de TX
     * para no retener conexiones Hikari durante I/O.
     */
    @Override
    @Async
    public void processJobAsync(UUID jobId) {
        ReportExportJob job = transactions.required(() -> {
            ReportExportJob found = repositoryPort.findById(jobId).orElse(null);
            if (found == null) {
                return null;
            }
            found.markProcessing();
            return repositoryPort.save(found);
        });
        if (job == null) {
            return;
        }

        try {
            ReportGeneratorPort generator = reportGenerators.stream()
                    .filter(g -> g.supports(job.getFormat()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported format: " + job.getFormat()));

            List<ObservationSummary> rows = transactions.readOnly(() -> {
                try (Stream<ObservationSummary> dataStream =
                             queryPort.streamAllObservationsForReport(job.getProgramId(), job.getPhaseId())) {
                    return dataStream.toList();
                }
            });

            File file = generator.generateReport(rows.stream(), job.getFormat());
            transactions.required(() -> {
                job.markCompleted(file.getAbsolutePath());
                repositoryPort.save(job);
            });
        } catch (Exception ex) {
            transactions.required(() -> {
                job.markFailed(ex.getMessage());
                repositoryPort.save(job);
            });
        }
    }

    @Override
    public ReportExportJob getJobStatus(UUID jobId, UUID requestingUserId) {
        return transactions.readOnly(() -> loadOwnedJob(jobId, requestingUserId));
    }

    @Override
    public InputStream getJobFileStream(UUID jobId, UUID requestingUserId) {
        ReportExportJob job = getJobStatus(jobId, requestingUserId);

        if (job.getStatus() != JobStatus.COMPLETED || job.getFilePath() == null) {
            throw new InvalidJobStateException("The report has not been generated yet.");
        }

        File file = new File(job.getFilePath());
        if (!file.exists()) {
            throw new InvalidJobStateException("The generated file was not found on the server.");
        }

        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new InvalidJobStateException("Could not read export file.");
        }
    }

    private ReportExportJob loadOwnedJob(UUID jobId, UUID requestingUserId) {
        ReportExportJob job = repositoryPort.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));

        if (!job.getUserId().equals(requestingUserId)) {
            throw new InvalidJobStateException("Access denied to requested job");
        }
        return job;
    }
}
