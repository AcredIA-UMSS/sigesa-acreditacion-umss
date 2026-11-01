package com.umss.sigesa.application.service.dossier;

import com.umss.sigesa.application.port.out.DossierExportPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.ReportExportJobRepositoryPort;
import com.umss.sigesa.application.port.out.TransactionRunnerPort;
import com.umss.sigesa.domain.model.JobStatus;
import com.umss.sigesa.domain.model.ReportExportJob;
import com.umss.sigesa.domain.model.ReportFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
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
        service = new DossierExportService(repositoryPort, transactions, dossierExportPort, blobStorage);
    }

    @Test
    @DisplayName("enqueueJob crea job PENDING con formato ZIP para el proceso")
    void enqueueJobCreatesPendingZipJob() {
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();

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
    @DisplayName("processJobAsync genera ZIP, almacena blob y marca COMPLETED")
    void processJobAsyncCompletesSuccessfully() throws Exception {
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

        File temp = Files.createTempFile("test", ".zip").toFile();
        Files.write(temp.toPath(), "ZIP".getBytes());
        when(dossierExportPort.generateDossierZip(eq(processId), eq(jobId.toString()))).thenReturn(temp);
        when(blobStorage.store(eq(jobId), eq(1), any(byte[].class), anyString())).thenReturn("dossier-exports/test.zip");

        service.processJobAsync(jobId);

        assertEquals(JobStatus.COMPLETED, job.getStatus());
        assertEquals("dossier-exports/test.zip", job.getFilePath());
        verify(dossierExportPort).generateDossierZip(processId, jobId.toString());
        verify(blobStorage).store(eq(jobId), eq(1), any(byte[].class), anyString());
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
        when(dossierExportPort.generateDossierZip(any(), any())).thenThrow(new RuntimeException("boom"));

        service.processJobAsync(jobId);

        assertEquals(JobStatus.FAILED, job.getStatus());
        assertNotNull(job.getErrorMessage());
    }
}
