package com.umss.sigesa.application.service.report;

import com.umss.sigesa.application.port.out.ReportJobRepositoryPort;
import com.umss.sigesa.domain.exception.ReportAccessDeniedException;
import com.umss.sigesa.domain.exception.ReportJobNotFoundException;
import com.umss.sigesa.domain.model.ExecutiveReportFilters;
import com.umss.sigesa.domain.model.ReportJob;
import com.umss.sigesa.domain.model.ReportJobStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetReportJobStatusService — FSD-UC-014")
class GetReportJobStatusServiceTest {

    @Mock
    private ReportJobRepositoryPort reportJobRepository;

    @InjectMocks
    private GetReportJobStatusService service;

    @Test
    void getStatus_returnsDownloadPathWhenCompleted() {
        UUID jobId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        ReportJob job = ReportJob.createPending(jobId, requesterId, new ExecutiveReportFilters(null, null, 2026));
        job.markCompleted("file.pdf");

        when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        var status = service.getStatus(jobId, requesterId);

        assertEquals(ReportJobStatus.COMPLETED, status.status());
        assertEquals("/api/v1/reports/executive/pdf/" + jobId + "/download", status.downloadPath());
    }

    @Test
    void getStatus_deniesForeignRequester() {
        UUID jobId = UUID.randomUUID();
        ReportJob job = ReportJob.createPending(jobId, UUID.randomUUID(), new ExecutiveReportFilters(null, null, 2026));
        when(reportJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThrows(ReportAccessDeniedException.class,
                () -> service.getStatus(jobId, UUID.randomUUID()));
    }

    @Test
    void getStatus_throwsWhenMissing() {
        UUID jobId = UUID.randomUUID();
        when(reportJobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThrows(ReportJobNotFoundException.class,
                () -> service.getStatus(jobId, UUID.randomUUID()));
    }
}
