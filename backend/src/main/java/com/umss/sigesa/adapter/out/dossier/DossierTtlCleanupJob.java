package com.umss.sigesa.adapter.out.dossier;

import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.ReportExportJobRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Tarea periódica y de arranque para la purga de exportaciones ZIP de dossier expiradas (TTL 24-48h)
 * y recuperación de jobs zombies/estancados tras caídas o reinicios del servidor.
 */
@Component
public class DossierTtlCleanupJob implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DossierTtlCleanupJob.class);

    private final EvidenceBlobStoragePort blobStorage;
    private final ReportExportJobRepositoryPort repositoryPort;
    private final Duration ttlDuration;
    private final Duration staleThreshold;

    public DossierTtlCleanupJob(
            EvidenceBlobStoragePort blobStorage,
            ReportExportJobRepositoryPort repositoryPort,
            @Value("${sigesa.dossier.ttl-hours:24}") int ttlHours,
            @Value("${sigesa.dossier.job-timeout-minutes:10}") int jobTimeoutMinutes) {
        this.blobStorage = blobStorage;
        this.repositoryPort = repositoryPort;
        this.ttlDuration = Duration.ofHours(ttlHours);
        this.staleThreshold = Duration.ofMinutes(Math.max(15, jobTimeoutMinutes * 2));
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("[DossierTtlCleanupJob] Ejecutando purga inicial de exportaciones y desbloqueo de jobs zombies...");
        cleanupExpiredDossiers();
    }

    @Scheduled(cron = "${sigesa.dossier.cleanup-cron:0 0 * * * *}")
    public void scheduleCleanup() {
        log.debug("[DossierTtlCleanupJob] Ejecutando purga programada de exportaciones expiradas...");
        cleanupExpiredDossiers();
    }

    public void cleanupExpiredDossiers() {
        try {
            blobStorage.purgeExpiredDossierExports(ttlDuration);
        } catch (Exception ex) {
            log.warn("[DossierTtlCleanupJob] Error durante la purga de exportaciones: {}", ex.getMessage());
        }

        try {
            int unblocked = repositoryPort.markStaleJobsAsFailed(
                    staleThreshold, "Job interrumpido por reinicio del servidor o timeout excedido");
            if (unblocked > 0) {
                log.info("[DossierTtlCleanupJob] Se recuperaron y marcaron como FAILED {} jobs estancados", unblocked);
            }
        } catch (Exception ex) {
            log.warn("[DossierTtlCleanupJob] Error durante la recuperación de jobs estancados: {}", ex.getMessage());
        }
    }
}
