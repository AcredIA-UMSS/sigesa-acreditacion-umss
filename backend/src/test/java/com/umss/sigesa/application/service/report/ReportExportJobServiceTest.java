package com.umss.sigesa.application.service.report;

import com.umss.sigesa.application.port.out.DashboardQueryPort;
import com.umss.sigesa.application.port.out.ReportExportJobRepositoryPort;
import com.umss.sigesa.application.port.out.ReportGeneratorPort;
import com.umss.sigesa.domain.exception.InvalidJobStateException;
import com.umss.sigesa.domain.exception.JobNotFoundException;
import com.umss.sigesa.domain.model.JobStatus;
import com.umss.sigesa.domain.model.ObservationSummary;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportExportJobService — PR-IMPL-011 / DD-UC-011")
class ReportExportJobServiceTest {

    @Mock
    private ReportExportJobRepositoryPort repositoryPort;
    @Mock
    private DashboardQueryPort queryPort;
    @Mock
    private ReportGeneratorPort generatorPort;

    private ReportExportJobService service;

    @BeforeEach
    void setUp() {
        service = new ReportExportJobService(repositoryPort, queryPort, List.of(generatorPort));
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
    }

    @Test
    @DisplayName("processJobAsync: Procesa trabajo exitosamente")
    void processJobAsync_exitoso() throws IOException {
        UUID jobId = UUID.randomUUID();
        ReportExportJob job = ReportExportJob.createNew(UUID.randomUUID(), UUID.randomUUID(), ReportFormat.XLSX, 1);
        File tempFile = File.createTempFile("test_", ".xlsx");
        tempFile.deleteOnExit();

        when(repositoryPort.findById(jobId)).thenReturn(Optional.of(job));
        when(generatorPort.supports(ReportFormat.XLSX)).thenReturn(true);
        when(queryPort.streamAllObservationsForReport(any(), any())).thenReturn(Stream.empty());
        when(generatorPort.generateReport(any(), any())).thenReturn(tempFile);

        service.processJobAsync(jobId);

        assertEquals(JobStatus.COMPLETED, job.getStatus());
        assertEquals(100, job.getProgressPercentage());
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
        File tempFile = File.createTempFile("test_done_", ".xlsx");
        tempFile.deleteOnExit();

        ReportExportJob job = ReportExportJob.createNew(userId, UUID.randomUUID(), ReportFormat.XLSX, 1);
        job.markCompleted(tempFile.getAbsolutePath());

        when(repositoryPort.findById(job.getJobId())).thenReturn(Optional.of(job));

        InputStream is = service.getJobFileStream(job.getJobId(), userId);
        assertNotNull(is);
        is.close();
    }
}
