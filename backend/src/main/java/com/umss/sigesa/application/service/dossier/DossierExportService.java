package com.umss.sigesa.application.service.dossier;

import com.umss.sigesa.application.port.in.ExportProcessDossierUseCase;
import com.umss.sigesa.application.port.in.GetProcessDossierExportJobStatusUseCase;
import com.umss.sigesa.application.port.out.DossierExportPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.ReportExportJobRepositoryPort;
import com.umss.sigesa.application.port.out.TransactionRunnerPort;
import com.umss.sigesa.domain.exception.InvalidJobStateException;
import com.umss.sigesa.domain.exception.JobNotFoundException;
import com.umss.sigesa.domain.model.JobStatus;
import com.umss.sigesa.domain.model.ReportExportJob;
import com.umss.sigesa.domain.model.ReportFormat;
import org.springframework.scheduling.annotation.Async;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

public class DossierExportService implements ExportProcessDossierUseCase, GetProcessDossierExportJobStatusUseCase {

    private final ReportExportJobRepositoryPort repositoryPort;
    private final TransactionRunnerPort transactions;
    private final DossierExportPort dossierExportPort;
    private final EvidenceBlobStoragePort blobStorage;

    public DossierExportService(ReportExportJobRepositoryPort repositoryPort,
                                TransactionRunnerPort transactions,
                                DossierExportPort dossierExportPort,
                                EvidenceBlobStoragePort blobStorage) {
        this.repositoryPort = repositoryPort;
        this.transactions = transactions;
        this.dossierExportPort = dossierExportPort;
        this.blobStorage = blobStorage;
    }

    @Override
    public ReportExportJob enqueueJob(UUID userId, UUID processId) {
        ReportExportJob job = new ReportExportJob(
                UUID.randomUUID(),
                userId,
                processId,
                ReportFormat.ZIP,
                null,
                JobStatus.PENDING,
                0,
                null,
                null,
                java.time.Instant.now(),
                java.time.Instant.now()
        );
        return transactions.required(() -> repositoryPort.save(job));
    }

    @Override
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
            File file = dossierExportPort.generateDossierZip(job.getProgramId(), job.getJobId().toString());
            byte[] content = Files.readAllBytes(file.toPath());
            String storageKey = blobStorage.store(job.getJobId(), 1, content, file.getName());
            Files.deleteIfExists(file.toPath());
            ReportExportJob completedJob = job;
            transactions.required(() -> {
                completedJob.markCompleted(storageKey);
                repositoryPort.save(completedJob);
                return null;
            });
        } catch (Exception ex) {
            ReportExportJob failedJob = job;
            transactions.required(() -> {
                failedJob.markFailed(ex.getMessage());
                repositoryPort.save(failedJob);
                return null;
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

        byte[] content = blobStorage.retrieve(job.getFilePath())
                .orElseThrow(() -> new InvalidJobStateException("The generated file was not found on the server."));
        return new ByteArrayInputStream(content);
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
