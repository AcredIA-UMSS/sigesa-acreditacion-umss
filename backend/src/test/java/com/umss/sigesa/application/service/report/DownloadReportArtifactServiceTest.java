package com.umss.sigesa.application.service.report;

import com.umss.sigesa.application.port.out.ReportArtifactStoragePort;
import com.umss.sigesa.application.port.out.ReportJobRepositoryPort;
import com.umss.sigesa.domain.exception.ReportAccessDeniedException;
import com.umss.sigesa.domain.exception.ReportJobNotFoundException;
import com.umss.sigesa.domain.exception.ReportNotReadyException;
import com.umss.sigesa.domain.model.ExecutiveReportFilters;
import com.umss.sigesa.domain.model.ReportJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DownloadReportArtifactService — FSD-UC-014")
class DownloadReportArtifactServiceTest {

    @Mock
    private ReportJobRepositoryPort reportJobRepository;
    @Mock
    private ReportArtifactStoragePort artifactStorage;

    @InjectMocks
    private DownloadReportArtifactService service;

    @Test
    void shouldDownloadCompletedArtifactForRequester() {
        UUID jobId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        ReportJob job = ReportJob.createPending(jobId, requesterId, new ExecutiveReportFilters(null, null, 2026));
        job.markCompleted("reports/exec.pdf");
        byte[] pdf = new byte[]{37, 80, 68, 70};

        when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(artifactStorage.retrieve("reports/exec.pdf")).thenReturn(Optional.of(pdf));

        var artifact = service.download(jobId, requesterId);

        assertThat(artifact.content()).isEqualTo(pdf);
        assertThat(artifact.filename()).isEqualTo("sigesa-reporte-ejecutivo-" + jobId + ".pdf");
        verify(artifactStorage).retrieve("reports/exec.pdf");
    }

    @Test
    void shouldThrowWhenJobDoesNotExist() {
        UUID jobId = UUID.randomUUID();
        when(reportJobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.download(jobId, UUID.randomUUID()))
                .isInstanceOf(ReportJobNotFoundException.class);
        verify(artifactStorage, never()).retrieve(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectForeignRequester() {
        UUID jobId = UUID.randomUUID();
        ReportJob job = ReportJob.createPending(jobId, UUID.randomUUID(), new ExecutiveReportFilters(null, null, 2026));
        job.markCompleted("reports/exec.pdf");
        when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.download(jobId, UUID.randomUUID()))
                .isInstanceOf(ReportAccessDeniedException.class);
        verify(artifactStorage, never()).retrieve(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldThrowWhenJobIsStillPending() {
        UUID jobId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        ReportJob job = ReportJob.createPending(jobId, requesterId, new ExecutiveReportFilters(null, null, 2026));
        when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.download(jobId, requesterId))
                .isInstanceOf(ReportNotReadyException.class);
        verify(artifactStorage, never()).retrieve(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldThrowWhenJobFailed() {
        UUID jobId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        ReportJob job = ReportJob.createPending(jobId, requesterId, new ExecutiveReportFilters(null, null, 2026));
        job.markFailed("REPORT_GENERATION_FAILED");
        when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.download(jobId, requesterId))
                .isInstanceOf(ReportNotReadyException.class);
        verify(artifactStorage, never()).retrieve(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldThrowWhenCompletedJobHasNoArtifactKey() {
        UUID jobId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        ReportJob job = ReportJob.createPending(jobId, requesterId, new ExecutiveReportFilters(null, null, 2026));
        job.markCompleted(null);
        when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.download(jobId, requesterId))
                .isInstanceOf(ReportNotReadyException.class);
        verify(artifactStorage, never()).retrieve(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldThrowWhenStoredArtifactIsMissing() {
        UUID jobId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        ReportJob job = ReportJob.createPending(jobId, requesterId, new ExecutiveReportFilters(null, null, 2026));
        job.markCompleted("missing.pdf");
        when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(artifactStorage.retrieve("missing.pdf")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.download(jobId, requesterId))
                .isInstanceOf(ReportJobNotFoundException.class);
    }
}
