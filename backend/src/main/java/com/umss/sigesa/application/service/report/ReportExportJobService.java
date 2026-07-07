package com.umss.sigesa.application.service.report;

import com.umss.sigesa.application.port.in.ExportReportJobUseCase;
import com.umss.sigesa.application.port.in.GetReportJobStatusUseCase;
import com.umss.sigesa.application.port.out.DashboardQueryPort;
import com.umss.sigesa.application.port.out.ReportExportJobRepositoryPort;
import com.umss.sigesa.application.port.out.ReportGeneratorPort;
import com.umss.sigesa.domain.exception.InvalidJobStateException;
import com.umss.sigesa.domain.exception.JobNotFoundException;
import com.umss.sigesa.domain.model.JobStatus;
import com.umss.sigesa.domain.model.ObservationSummary;
import com.umss.sigesa.domain.model.ReportExportJob;
import com.umss.sigesa.domain.model.ReportFormat;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class ReportExportJobService implements ExportReportJobUseCase, GetReportJobStatusUseCase {

    private final ReportExportJobRepositoryPort repositoryPort;
    private final DashboardQueryPort queryPort;
    private final List<ReportGeneratorPort> reportGenerators;

    public ReportExportJobService(ReportExportJobRepositoryPort repositoryPort,
                                  DashboardQueryPort queryPort,
                                  List<ReportGeneratorPort> reportGenerators) {
        this.repositoryPort = repositoryPort;
        this.queryPort = queryPort;
        this.reportGenerators = reportGenerators;
    }

    @Override
    @Transactional
    public ReportExportJob enqueueJob(UUID userId, UUID programId, ReportFormat format, Integer phaseId) {
        ReportExportJob job = ReportExportJob.createNew(userId, programId, format, phaseId);
        return repositoryPort.save(job);
    }

    @Override
    @Async
    @Transactional
    public void processJobAsync(UUID jobId) {
        ReportExportJob job = repositoryPort.findById(jobId).orElse(null);
        if (job == null) {
            return;
        }

        try {
            job.markProcessing();
            repositoryPort.save(job);

            ReportGeneratorPort generator = reportGenerators.stream()
                    .filter(g -> g.supports(job.getFormat()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported format: " + job.getFormat()));

            try (Stream<ObservationSummary> dataStream = queryPort.streamAllObservationsForReport(job.getProgramId(), job.getPhaseId())) {
                File file = generator.generateReport(dataStream, job.getFormat());
                job.markCompleted(file.getAbsolutePath());
            }

            repositoryPort.save(job);
        } catch (Exception ex) {
            job.markFailed(ex.getMessage());
            repositoryPort.save(job);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReportExportJob getJobStatus(UUID jobId, UUID requestingUserId) {
        ReportExportJob job = repositoryPort.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));

        if (!job.getUserId().equals(requestingUserId)) {
            throw new InvalidJobStateException("Access denied to requested job");
        }

        return job;
    }

    @Override
    @Transactional(readOnly = true)
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
}
