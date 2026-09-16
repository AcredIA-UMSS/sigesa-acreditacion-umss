package com.umss.sigesa.application.service.report;

import com.umss.sigesa.application.port.out.ExecutiveDataPort;
import com.umss.sigesa.application.port.out.PdfRendererPort;
import com.umss.sigesa.application.port.out.ReportArtifactStoragePort;
import com.umss.sigesa.application.port.out.ReportJobRepositoryPort;
import com.umss.sigesa.domain.exception.ReportTemplateException;
import com.umss.sigesa.domain.model.ExecutiveReportFilters;
import com.umss.sigesa.domain.model.ExecutiveReportSnapshot;
import com.umss.sigesa.domain.model.ReportJob;
import com.umss.sigesa.domain.model.ReportJobStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessReportJobService — FSD-UC-014")
class ProcessReportJobServiceTest {

    @Mock
    private ReportJobRepositoryPort reportJobRepository;
    @Mock
    private ExecutiveDataPort executiveDataPort;
    @Mock
    private PdfRendererPort pdfRendererPort;
    @Mock
    private ReportArtifactStoragePort artifactStorage;

    @InjectMocks
    private ProcessReportJobService service;

    @Test
    @DisplayName("Escenario: Job completado produce artefacto PDF")
    void process_marksCompletedOnSuccess() {
        UUID jobId = UUID.randomUUID();
        ReportJob job = ReportJob.createPending(jobId, UUID.randomUUID(), new ExecutiveReportFilters(null, null, 2026));
        ExecutiveReportSnapshot snapshot = new ExecutiveReportSnapshot(
                LocalDateTime.now(), job.getFilters(), List.of());

        when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(executiveDataPort.fetchSnapshot(job.getFilters())).thenReturn(snapshot);
        when(pdfRendererPort.render(snapshot)).thenReturn(new byte[]{1, 2, 3});
        when(artifactStorage.store(jobId, new byte[]{1, 2, 3})).thenReturn("artifact.pdf");
        when(reportJobRepository.save(any(ReportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.process(jobId);

        assertEquals(ReportJobStatus.COMPLETED, job.getStatus());
        assertEquals("artifact.pdf", job.getArtifactKey());
    }

    @Test
    @DisplayName("Escenario: Error de plantilla marca FAILED con REPORT_TEMPLATE")
    void process_marksFailedOnTemplateError() {
        UUID jobId = UUID.randomUUID();
        ReportJob job = ReportJob.createPending(jobId, UUID.randomUUID(), new ExecutiveReportFilters(null, null, 2026));

        when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(executiveDataPort.fetchSnapshot(job.getFilters()))
                .thenReturn(new ExecutiveReportSnapshot(LocalDateTime.now(), job.getFilters(), List.of()));
        when(pdfRendererPort.render(any())).thenThrow(new ReportTemplateException("fail", null));
        when(reportJobRepository.save(any(ReportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.process(jobId);

        assertEquals(ReportJobStatus.FAILED, job.getStatus());
        assertEquals(ProcessReportJobService.ERROR_REPORT_TEMPLATE, job.getErrorCode());
    }

    @Test
    @DisplayName("Escenario: Error inesperado marca FAILED con REPORT_GENERATION_FAILED")
    void process_marksFailedOnUnexpectedError() {
        UUID jobId = UUID.randomUUID();
        ReportJob job = ReportJob.createPending(jobId, UUID.randomUUID(), new ExecutiveReportFilters(null, null, 2026));

        when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(executiveDataPort.fetchSnapshot(job.getFilters()))
                .thenThrow(new IllegalStateException("storage unavailable"));
        when(reportJobRepository.save(any(ReportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.process(jobId);

        assertEquals(ReportJobStatus.FAILED, job.getStatus());
        assertEquals(ProcessReportJobService.ERROR_GENERATION_FAILED, job.getErrorCode());
    }

    @Test
    void shouldThrowWhenJobDoesNotExist() {
        UUID jobId = UUID.randomUUID();
        when(reportJobRepository.findById(jobId)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(com.umss.sigesa.domain.exception.ReportJobNotFoundException.class,
                () -> service.process(jobId));
        verify(executiveDataPort, org.mockito.Mockito.never()).fetchSnapshot(any());
        verify(pdfRendererPort, org.mockito.Mockito.never()).render(any());
        verify(artifactStorage, org.mockito.Mockito.never()).store(any(), any());
        verify(reportJobRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void shouldNotStoreArtifactWhenRendererFails() {
        UUID jobId = UUID.randomUUID();
        ReportJob job = ReportJob.createPending(jobId, UUID.randomUUID(), new ExecutiveReportFilters(null, null, 2026));

        when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(executiveDataPort.fetchSnapshot(job.getFilters()))
                .thenReturn(new ExecutiveReportSnapshot(LocalDateTime.now(), job.getFilters(), List.of()));
        when(pdfRendererPort.render(any())).thenThrow(new ReportTemplateException("fail", null));
        when(reportJobRepository.save(any(ReportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.process(jobId);

        verify(artifactStorage, org.mockito.Mockito.never()).store(any(), any());
        verify(reportJobRepository, org.mockito.Mockito.times(2)).save(job);
    }

    @Test
    void shouldCompleteWithEmptySnapshot() {
        UUID jobId = UUID.randomUUID();
        ReportJob job = ReportJob.createPending(jobId, UUID.randomUUID(), new ExecutiveReportFilters(null, null, 2026));
        ExecutiveReportSnapshot snapshot = new ExecutiveReportSnapshot(
                LocalDateTime.now(), job.getFilters(), List.of());

        when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(executiveDataPort.fetchSnapshot(job.getFilters())).thenReturn(snapshot);
        when(pdfRendererPort.render(snapshot)).thenReturn(new byte[0]);
        when(artifactStorage.store(jobId, new byte[0])).thenReturn("empty.pdf");
        when(reportJobRepository.save(any(ReportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.process(jobId);

        assertEquals(ReportJobStatus.COMPLETED, job.getStatus());
        assertEquals("empty.pdf", job.getArtifactKey());
    }
}
