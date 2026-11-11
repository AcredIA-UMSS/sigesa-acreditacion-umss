package com.umss.sigesa.application.service.dossier;

import com.umss.sigesa.application.port.out.DossierExportPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.ReportExportJobRepositoryPort;
import com.umss.sigesa.application.port.out.TransactionRunnerPort;
import com.umss.sigesa.domain.exception.ActiveExportAlreadyExistsException;
import com.umss.sigesa.domain.exception.TooManyConcurrentExportsException;
import com.umss.sigesa.domain.model.JobStatus;
import com.umss.sigesa.domain.model.ReportExportJob;
import com.umss.sigesa.domain.model.ReportFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DossierExportServiceTest {

    @Mock
    private ReportExportJobRepositoryPort repositoryPort;

    @Mock
    private TransactionRunnerPort transactions;

    @Mock
    private DossierExportPort dossierExportPort;

    @Mock
    private EvidenceBlobStoragePort blobStorage;

    private DossierExportService service;

    @BeforeEach
    void setUp() {
        service = new DossierExportService(repositoryPort, transactions, dossierExportPort, blobStorage, 2, 10);
    }

    @Test
    @DisplayName("enqueueJob crea job PENDING con formato ZIP para el proceso")
    void enqueueJobCreatesPendingZipJob() {
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();

        when(transactions.readOnly(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        when(repositoryPort.hasActiveJobForUser(userId)).thenReturn(false);

        when(transactions.required(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        when(repositoryPort.save(any(ReportExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportExportJob job = service.enqueueJob(userId, processId);

        assertNotNull(job.getJobId());
        assertEquals(userId, job.getUserId());
        assertEquals(processId, job.getProgramId());
        assertEquals(ReportFormat.ZIP, job.getFormat());
        assertEquals(JobStatus.PENDING, job.getStatus());
        verify(repositoryPort).save(any(ReportExportJob.class));
    }

    @Test
    @DisplayName("enqueueJob rechaza si el usuario ya tiene un job activo")
    void enqueueJobRejectsWhenUserHasActiveJob() {
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();

        when(transactions.readOnly(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        when(repositoryPort.hasActiveJobForUser(userId)).thenReturn(true);

        assertThrows(ActiveExportAlreadyExistsException.class, () -> service.enqueueJob(userId, processId));
        verify(repositoryPort, never()).save(any(ReportExportJob.class));
    }

    @Test
    @DisplayName("enqueueJob rechaza con TooManyConcurrentExportsException si se satura el semáforo")
    void enqueueJobRejectsWhenSemaphoreFull() {
        when(transactions.readOnly(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        when(repositoryPort.hasActiveJobForUser(any())).thenReturn(false);

        when(transactions.required(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        when(repositoryPort.save(any(ReportExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Capacity is 2
        service.enqueueJob(UUID.randomUUID(), UUID.randomUUID());
        service.enqueueJob(UUID.randomUUID(), UUID.randomUUID());

        // 3rd should throw
        assertThrows(TooManyConcurrentExportsException.class, () ->
                service.enqueueJob(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    @DisplayName("processJobAsync genera ZIP en streaming y marca COMPLETED")
    void processJobAsyncCompletesSuccessfully() {
        UUID jobId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        ReportExportJob job = new ReportExportJob(jobId, userId, processId, ReportFormat.ZIP, null,
                JobStatus.PENDING, 0, null, null, Instant.now(), Instant.now());

        when(transactions.required(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        when(repositoryPort.findById(jobId)).thenReturn(Optional.of(job));
        when(repositoryPort.save(any(ReportExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dossierExportPort.generateDossierZip(eq(processId), eq(jobId.toString()), any()))
                .thenReturn("dossier-exports/test.zip");

        service.processJobAsync(jobId);

        assertEquals(JobStatus.COMPLETED, job.getStatus());
        assertEquals("dossier-exports/test.zip", job.getFilePath());
        verify(dossierExportPort).generateDossierZip(eq(processId), eq(jobId.toString()), any());
    }

    @Test
    @DisplayName("processJobAsync marca FAILED si ocurre error")
    void processJobAsyncFailsOnError() {
        UUID jobId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        ReportExportJob job = new ReportExportJob(jobId, userId, processId, ReportFormat.ZIP, null,
                JobStatus.PENDING, 0, null, null, Instant.now(), Instant.now());

        when(transactions.required(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        when(repositoryPort.findById(jobId)).thenReturn(Optional.of(job));
        when(repositoryPort.save(any(ReportExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dossierExportPort.generateDossierZip(any(), any(), any())).thenThrow(new RuntimeException("boom"));

        service.processJobAsync(jobId);

        assertEquals(JobStatus.FAILED, job.getStatus());
        assertNotNull(job.getErrorMessage());
    }

    @Test
    @DisplayName("getPresignedDownloadUrl delega al blobStorage")
    void getPresignedDownloadUrlDelegatesToStorage() {
        UUID jobId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        ReportExportJob job = new ReportExportJob(jobId, userId, processId, ReportFormat.ZIP, null,
                JobStatus.COMPLETED, 100, "dossier-exports/test.zip", null, Instant.now(), Instant.now());

        when(transactions.readOnly(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        when(repositoryPort.findById(jobId)).thenReturn(Optional.of(job));
        when(blobStorage.generatePresignedDownloadUrl(eq("dossier-exports/test.zip"), eq(Duration.ofMinutes(15))))
                .thenReturn(Optional.of("https://s3.amazonaws.com/test-bucket/dossier-exports/test.zip?signature=xyz"));

        Optional<String> presigned = service.getPresignedDownloadUrl(jobId, userId);

        assertTrue(presigned.isPresent());
        assertTrue(presigned.get().contains("https://s3.amazonaws.com/test-bucket"));
    }

    @Test
    @DisplayName("getJobFileStream retorna stream de bytes cuando COMPLETED")
    void getJobFileStreamReturnsContent() {
        UUID jobId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        ReportExportJob job = new ReportExportJob(jobId, userId, processId, ReportFormat.ZIP, null,
                JobStatus.COMPLETED, 100, "dossier-exports/test.zip", null, Instant.now(), Instant.now());

        when(transactions.readOnly(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        when(repositoryPort.findById(jobId)).thenReturn(Optional.of(job));
        when(blobStorage.openInputStream("dossier-exports/test.zip")).thenReturn(Optional.of(new java.io.ByteArrayInputStream("DUMMY_ZIP".getBytes())));

        InputStream stream = service.getJobFileStream(jobId, userId);
        assertNotNull(stream);
    }
}
