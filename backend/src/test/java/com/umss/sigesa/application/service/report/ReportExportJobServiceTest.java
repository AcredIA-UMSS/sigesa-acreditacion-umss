package com.umss.sigesa.application.service.report;

import com.umss.sigesa.application.port.out.DashboardQueryPort;
import com.umss.sigesa.application.port.out.DirectTransactionRunner;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.ReportExportJobRepositoryPort;
import com.umss.sigesa.application.port.out.ReportGeneratorPort;
import com.umss.sigesa.application.port.out.TransactionRunnerPort;
import com.umss.sigesa.domain.exception.InvalidJobStateException;
import com.umss.sigesa.domain.exception.JobNotFoundException;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportExportJobService — PR-IMPL-011 / PR-IMPL-040")
class ReportExportJobServiceTest {

    @Mock
    private ReportExportJobRepositoryPort repositoryPort;
    @Mock
    private DashboardQueryPort queryPort;
    @Mock
    private ReportGeneratorPort generatorPort;
    @Mock
    private EvidenceBlobStoragePort blobStorage;

    private ReportExportJobService service;
    private RecordingTransactionRunner transactions;

    @BeforeEach
    void setUp() {
        transactions = new RecordingTransactionRunner();
        service = new ReportExportJobService(
                repositoryPort, queryPort, List.of(generatorPort), transactions, blobStorage);
    }

    @Test
    @DisplayName("enqueueJob: Crea y guarda un nuevo trabajo de exportacion")
    void enqueueJob_exitoso() {
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        when(repositoryPort.save(any(ReportExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportExportJob job = service.enqueueJob(userId, programId, ReportFormat.XLSX, 1);

        assertNotNull(job);
        assertEquals(userId, job.getUserId());
        assertEquals(programId, job.getProgramId());
        assertEquals(JobStatus.PENDING, job.getStatus());
        verify(repositoryPort).save(any(ReportExportJob.class));
        assertTrue(transactions.requiredCalls >= 1);
    }

    @Test
    @DisplayName("processJobAsync: Procesa trabajo exitosamente")
    void processJobAsync_exitoso() throws IOException {
        UUID jobId = UUID.randomUUID();
        ReportExportJob job = ReportExportJob.createNew(UUID.randomUUID(), UUID.randomUUID(), ReportFormat.XLSX, 1);
        File tempFile = File.createTempFile("test_", ".xlsx");
        tempFile.deleteOnExit();

        when(repositoryPort.findById(jobId)).thenReturn(Optional.of(job));
        when(repositoryPort.save(any(ReportExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(generatorPort.supports(ReportFormat.XLSX)).thenReturn(true);
        when(queryPort.streamAllObservationsForReport(any(), any())).thenReturn(Stream.empty());
        when(generatorPort.generateReport(any(), any())).thenReturn(tempFile);
        when(blobStorage.store(any(), eq(1), any(), any())).thenReturn("reports/job.xlsx");

        service.processJobAsync(jobId);

        assertEquals(JobStatus.COMPLETED, job.getStatus());
        assertEquals("reports/job.xlsx", job.getFilePath());
        assertEquals(100, job.getProgressPercentage());
    }

    @Test
    @DisplayName("processJobAsync: generateReport se ejecuta fuera de TX required (CON-03)")
    void processJobAsync_generatesFileOutsideWriteTransaction() throws IOException {
        UUID jobId = UUID.randomUUID();
        ReportExportJob job = ReportExportJob.createNew(UUID.randomUUID(), UUID.randomUUID(), ReportFormat.XLSX, 1);
        File tempFile = File.createTempFile("test_tx_", ".xlsx");
        tempFile.deleteOnExit();

        when(repositoryPort.findById(jobId)).thenReturn(Optional.of(job));
        when(repositoryPort.save(any(ReportExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(generatorPort.supports(ReportFormat.XLSX)).thenReturn(true);
        when(queryPort.streamAllObservationsForReport(any(), any())).thenReturn(Stream.empty());
        when(generatorPort.generateReport(any(), any())).thenAnswer(invocation -> {
            assertEquals(0, transactions.requiredDepth.get(),
                    "I/O de archivo no debe retener TX required (Hikari)");
            return tempFile;
        });
        when(blobStorage.store(any(), eq(1), any(), any())).thenReturn("reports/job.xlsx");

        service.processJobAsync(jobId);

        assertEquals(JobStatus.COMPLETED, job.getStatus());
        verify(generatorPort).generateReport(any(), eq(ReportFormat.XLSX));
        assertTrue(transactions.requiredCalls >= 2);
        assertTrue(transactions.readOnlyCalls >= 1);
    }

    @Test
    @DisplayName("processJobAsync: Maneja trabajo no encontrado sin lanzar excepcion")
    void processJobAsync_jobNoEncontrado() {
        UUID jobId = UUID.randomUUID();
        when(repositoryPort.findById(jobId)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.processJobAsync(jobId));
    }

    @Test
    @DisplayName("processJobAsync: Marca fallo si ocurre excepcion durante generacion")
    void processJobAsync_errorEnGeneracion() {
        UUID jobId = UUID.randomUUID();
        ReportExportJob job = ReportExportJob.createNew(UUID.randomUUID(), UUID.randomUUID(), ReportFormat.XLSX, 1);

        when(repositoryPort.findById(jobId)).thenReturn(Optional.of(job));
        when(repositoryPort.save(any(ReportExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(generatorPort.supports(ReportFormat.XLSX)).thenReturn(true);
        when(queryPort.streamAllObservationsForReport(any(), any())).thenThrow(new RuntimeException("DB error"));

        service.processJobAsync(jobId);

        assertEquals(JobStatus.FAILED, job.getStatus());
        assertEquals("DB error", job.getErrorMessage());
    }

    @Test
    @DisplayName("getJobStatus: Lanza JobNotFoundException si trabajo no existe")
    void getJobStatus_noEncontrado() {
        UUID jobId = UUID.randomUUID();
        when(repositoryPort.findById(jobId)).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class, () -> service.getJobStatus(jobId, UUID.randomUUID()));
    }

    @Test
    @DisplayName("getJobStatus: Lanza InvalidJobStateException si usuario no es dueño del trabajo")
    void getJobStatus_usuarioDistinto() {
        UUID jobId = UUID.randomUUID();
        ReportExportJob job = ReportExportJob.createNew(UUID.randomUUID(), UUID.randomUUID(), ReportFormat.XLSX, 1);
        when(repositoryPort.findById(jobId)).thenReturn(Optional.of(job));

        assertThrows(InvalidJobStateException.class, () -> service.getJobStatus(jobId, UUID.randomUUID()));
    }

    @Test
    @DisplayName("getJobFileStream: Retorna InputStream cuando el trabajo esta completado")
    void getJobFileStream_exitoso() throws IOException {
        UUID userId = UUID.randomUUID();

        ReportExportJob job = ReportExportJob.createNew(userId, UUID.randomUUID(), ReportFormat.XLSX, 1);
        job.markCompleted("reports/job.xlsx");

        when(repositoryPort.findById(job.getJobId())).thenReturn(Optional.of(job));
        when(blobStorage.retrieve("reports/job.xlsx")).thenReturn(Optional.of(new byte[]{1, 2}));

        InputStream is = service.getJobFileStream(job.getJobId(), userId);
        assertNotNull(is);
        is.close();
        assertEquals(0, transactions.requiredDepth.get());
    }

    @Test
    void shouldReturnJobStatusForOwner() {
        UUID userId = UUID.randomUUID();
        ReportExportJob job = ReportExportJob.createNew(userId, UUID.randomUUID(), ReportFormat.CSV, null);
        when(repositoryPort.findById(job.getJobId())).thenReturn(Optional.of(job));

        ReportExportJob result = service.getJobStatus(job.getJobId(), userId);

        assertEquals(JobStatus.PENDING, result.getStatus());
        verify(repositoryPort).findById(job.getJobId());
    }

    @Test
    void shouldThrowWhenReportHasNotBeenGeneratedYet() {
        UUID userId = UUID.randomUUID();
        ReportExportJob job = ReportExportJob.createNew(userId, UUID.randomUUID(), ReportFormat.PDF, 2);
        when(repositoryPort.findById(job.getJobId())).thenReturn(Optional.of(job));

        assertThrows(InvalidJobStateException.class,
                () -> service.getJobFileStream(job.getJobId(), userId));
    }

    @Test
    void shouldFailWhenFormatIsUnsupported() {
        UUID jobId = UUID.randomUUID();
        ReportExportJob job = ReportExportJob.createNew(UUID.randomUUID(), UUID.randomUUID(), ReportFormat.PDF, 1);
        when(repositoryPort.findById(jobId)).thenReturn(Optional.of(job));
        when(repositoryPort.save(any(ReportExportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(generatorPort.supports(ReportFormat.PDF)).thenReturn(false);

        service.processJobAsync(jobId);

        assertEquals(JobStatus.FAILED, job.getStatus());
        assertEquals("Unsupported format: PDF", job.getErrorMessage());
        verify(queryPort, never()).streamAllObservationsForReport(any(), any());
    }

    @Test
    void shouldThrowWhenGeneratedFileIsMissing() {
        UUID userId = UUID.randomUUID();
        ReportExportJob job = ReportExportJob.createNew(userId, UUID.randomUUID(), ReportFormat.XLSX, 1);
        job.markCompleted("reports/missing.xlsx");
        when(repositoryPort.findById(job.getJobId())).thenReturn(Optional.of(job));
        when(blobStorage.retrieve("reports/missing.xlsx")).thenReturn(Optional.empty());

        assertThrows(InvalidJobStateException.class,
                () -> service.getJobFileStream(job.getJobId(), userId));
    }

    /**
     * Cuenta profundidad de TX write para demostrar que el I/O no ocurre dentro de {@code required}.
     */
    static final class RecordingTransactionRunner implements TransactionRunnerPort {
        final AtomicInteger requiredDepth = new AtomicInteger();
        int requiredCalls;
        int readOnlyCalls;
        private final DirectTransactionRunner delegate = new DirectTransactionRunner();

        @Override
        public <T> T required(Supplier<T> action) {
            requiredCalls++;
            requiredDepth.incrementAndGet();
            try {
                return delegate.required(action);
            } finally {
                requiredDepth.decrementAndGet();
            }
        }

        @Override
        public void required(Runnable action) {
            requiredCalls++;
            requiredDepth.incrementAndGet();
            try {
                delegate.required(action);
            } finally {
                requiredDepth.decrementAndGet();
            }
        }

        @Override
        public <T> T readOnly(Supplier<T> action) {
            readOnlyCalls++;
            return delegate.readOnly(action);
        }
    }
}
