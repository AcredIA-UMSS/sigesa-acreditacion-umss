package com.umss.sigesa.adapter.out.dossier;

import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.ReportExportJobRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DossierTtlCleanupJobTest {

    @Mock
    private EvidenceBlobStoragePort blobStorage;

    @Mock
    private ReportExportJobRepositoryPort repositoryPort;

    @Test
    @DisplayName("run ejecuta purga en arranque con duración TTL y desbloquea jobs estancados")
    void runExecutesPurgeAtStartup() {
        DossierTtlCleanupJob job = new DossierTtlCleanupJob(blobStorage, repositoryPort, 48, 10);
        job.run(new DefaultApplicationArguments(new String[0]));

        verify(blobStorage).purgeExpiredDossierExports(Duration.ofHours(48));
        verify(repositoryPort).markStaleJobsAsFailed(eq(Duration.ofMinutes(20)), anyString());
    }

    @Test
    @DisplayName("scheduleCleanup ejecuta purga periódica y desbloquea jobs estancados")
    void scheduleCleanupExecutesPurge() {
        DossierTtlCleanupJob job = new DossierTtlCleanupJob(blobStorage, repositoryPort, 24, 10);
        job.scheduleCleanup();

        verify(blobStorage).purgeExpiredDossierExports(Duration.ofHours(24));
        verify(repositoryPort).markStaleJobsAsFailed(eq(Duration.ofMinutes(20)), anyString());
    }

    @Test
    @DisplayName("Maneja excepciones durante purga sin propagar error")
    void handlesExceptionsGracefully() {
        doThrow(new RuntimeException("S3 error")).when(blobStorage).purgeExpiredDossierExports(any());
        doThrow(new RuntimeException("DB error")).when(repositoryPort).markStaleJobsAsFailed(any(), anyString());

        DossierTtlCleanupJob job = new DossierTtlCleanupJob(blobStorage, repositoryPort, 24, 10);
        job.cleanupExpiredDossiers(); // Does not throw

        verify(blobStorage).purgeExpiredDossierExports(Duration.ofHours(24));
        verify(repositoryPort).markStaleJobsAsFailed(any(), anyString());
    }
}
