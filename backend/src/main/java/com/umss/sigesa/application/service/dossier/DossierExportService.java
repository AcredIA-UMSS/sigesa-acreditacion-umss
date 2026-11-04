package com.umss.sigesa.application.service.dossier;

import com.umss.sigesa.application.port.in.ExportProcessDossierUseCase;
import com.umss.sigesa.application.port.in.GetProcessDossierExportJobStatusUseCase;
import com.umss.sigesa.application.port.out.DossierExportPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.ReportExportJobRepositoryPort;
import com.umss.sigesa.application.port.out.TransactionRunnerPort;
import com.umss.sigesa.domain.exception.ActiveExportAlreadyExistsException;
import com.umss.sigesa.domain.exception.InvalidJobStateException;
import com.umss.sigesa.domain.exception.JobNotFoundException;
import com.umss.sigesa.domain.exception.TooManyConcurrentExportsException;
import com.umss.sigesa.domain.model.JobStatus;
import com.umss.sigesa.domain.model.ReportExportJob;
import com.umss.sigesa.domain.model.ReportFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DossierExportService implements ExportProcessDossierUseCase, GetProcessDossierExportJobStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(DossierExportService.class);

    private final ReportExportJobRepositoryPort repositoryPort;
    private final TransactionRunnerPort transactions;
    private final DossierExportPort dossierExportPort;
    private final EvidenceBlobStoragePort blobStorage;
    private final Semaphore activeJobsSemaphore;
    private final int jobTimeoutMinutes;
    private final ExecutorService zipWorkerExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final Set<UUID> activeJobPermits = ConcurrentHashMap.newKeySet();

    public DossierExportService(ReportExportJobRepositoryPort repositoryPort,
                                TransactionRunnerPort transactions,
                                DossierExportPort dossierExportPort,
                                EvidenceBlobStoragePort blobStorage) {
        this(repositoryPort, transactions, dossierExportPort, blobStorage, 4, 10);
    }

    public DossierExportService(ReportExportJobRepositoryPort repositoryPort,
                                TransactionRunnerPort transactions,
                                DossierExportPort dossierExportPort,
                                EvidenceBlobStoragePort blobStorage,
                                int maxConcurrentJobs,
                                int jobTimeoutMinutes) {
        this.repositoryPort = repositoryPort;
        this.transactions = transactions;
        this.dossierExportPort = dossierExportPort;
        this.blobStorage = blobStorage;
        this.activeJobsSemaphore = new Semaphore(maxConcurrentJobs);
        this.jobTimeoutMinutes = jobTimeoutMinutes;
    }

    @Override
    public ReportExportJob enqueueJob(UUID userId, UUID processId) {
        // 1. Límite por usuario: máximo 1 exportación activa
        if (transactions.readOnly(() -> repositoryPort.hasActiveJobForUser(userId))) {
            throw new ActiveExportAlreadyExistsException("El usuario ya tiene una exportación de dossier activa en proceso.");
        }

        // 2. Semáforo global de jobs activos
        if (!activeJobsSemaphore.tryAcquire()) {
            throw new TooManyConcurrentExportsException(
                    "Servicio de exportación ocupado: se alcanzó la concurrencia máxima permitida.", 60);
        }

        UUID jobId = UUID.randomUUID();
        try {
            ReportExportJob job = new ReportExportJob(
                    jobId,
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
            ReportExportJob saved = transactions.required(() -> repositoryPort.save(job));
            activeJobPermits.add(jobId);
            return saved;
        } catch (Exception ex) {
            activeJobsSemaphore.release();
            throw ex;
        }
    }

    @Override
    @Async("reportJobExecutor")
    public void processJobAsync(UUID jobId) {
        try {
            ReportExportJob job = transactions.required(() -> {
                ReportExportJob found = repositoryPort.findById(jobId).orElse(null);
                if (found == null) {
                    log.warn("[processJobAsync] Job {} no encontrado, abortando", jobId);
                    return null;
                }
                found.markProcessing();
                return repositoryPort.save(found);
            });
            if (job == null) {
                return;
            }

            log.info("[processJobAsync] Iniciando generación de ZIP en streaming para job {}", jobId);

            // Timeout de ejecución por job con interrupción cooperativa real en hilo virtual
            Future<String> generationFuture = zipWorkerExecutor.submit(() ->
                    dossierExportPort.generateDossierZip(job.getProgramId(), job.getJobId().toString(), progressPercent -> {
                        try {
                            transactions.required(() -> {
                                ReportExportJob current = repositoryPort.findById(jobId).orElse(null);
                                if (current != null && current.getStatus() == JobStatus.PROCESSING) {
                                    current.updateProgress(progressPercent);
                                    repositoryPort.save(current);
                                }
                                return null;
                            });
                        } catch (Exception pEx) {
                            log.debug("[processJobAsync] Progreso incremental no persistido: {}", pEx.getMessage());
                        }
                    }));

            String storageKey;
            try {
                storageKey = generationFuture.get(jobTimeoutMinutes, TimeUnit.MINUTES);
            } catch (TimeoutException te) {
                generationFuture.cancel(true);
                throw new IllegalStateException("Dossier export exceeded execution timeout of " + jobTimeoutMinutes + " minutes", te);
            }

            ReportExportJob completedJob = job;
            transactions.required(() -> {
                completedJob.markCompleted(storageKey);
                repositoryPort.save(completedJob);
                return null;
            });
            log.info("[processJobAsync] Job {} completado y almacenado en {}", jobId, storageKey);
        } catch (Exception ex) {
            log.error("[processJobAsync] Error procesando job {}: {}", jobId, ex.getMessage(), ex);
            try {
                transactions.required(() -> {
                    ReportExportJob failedJob = repositoryPort.findById(jobId)
                            .orElseThrow(() -> new JobNotFoundException(jobId));
                    failedJob.markFailed(ex.getMessage());
                    repositoryPort.save(failedJob);
                    return null;
                });
            } catch (Exception updateEx) {
                log.error("[processJobAsync] No se pudo marcar como FAILED el job {}: {}", jobId, updateEx.getMessage(), updateEx);
            }
        } finally {
            if (activeJobPermits.remove(jobId)) {
                activeJobsSemaphore.release();
            }
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

    @Override
    public Optional<String> getPresignedDownloadUrl(UUID jobId, UUID requestingUserId) {
        ReportExportJob job = getJobStatus(jobId, requestingUserId);
        if (job.getStatus() != JobStatus.COMPLETED || job.getFilePath() == null) {
            return Optional.empty();
        }
        return blobStorage.generatePresignedDownloadUrl(job.getFilePath(), Duration.ofMinutes(15));
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
