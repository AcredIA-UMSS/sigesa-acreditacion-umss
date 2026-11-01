package com.umss.sigesa.application.service.report;

import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.ReportJobRepositoryPort;
import com.umss.sigesa.domain.model.ExecutiveReportFilters;
import com.umss.sigesa.domain.model.ReportJob;
import com.umss.sigesa.domain.model.ReportJobStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateExecutiveReportService — FSD-UC-014")
class GenerateExecutiveReportServiceTest {

    @Mock
    private ReportJobRepositoryPort reportJobRepository;
    @Mock
    private AuditLogPort auditLogPort;
    @Mock
    private ReportJobProcessor reportJobProcessor;

    @InjectMocks
    private GenerateExecutiveReportService service;

    @Test
    @DisplayName("Escenario: Generación de reporte ejecutivo encola job PENDING")
    void generate_createsPendingJob() {
        UUID requesterId = UUID.randomUUID();
        ExecutiveReportFilters filters = new ExecutiveReportFilters(null, null, 2026);

        when(reportJobRepository.save(any(ReportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID jobId = service.generate(filters, requesterId);

        assertNotNull(jobId);

        ArgumentCaptor<ReportJob> captor = ArgumentCaptor.forClass(ReportJob.class);
        verify(reportJobRepository).save(captor.capture());
        assertEquals(ReportJobStatus.PENDING, captor.getValue().getStatus());
        verify(auditLogPort).logReportRequested(requesterId, jobId);
        verify(reportJobProcessor).enqueue(jobId);
    }

    @Test
    void shouldNotEnqueueWhenRepositorySaveFails() {
        UUID requesterId = UUID.randomUUID();
        ExecutiveReportFilters filters = new ExecutiveReportFilters(UUID.randomUUID(), UUID.randomUUID(), 2026);
        when(reportJobRepository.save(any(ReportJob.class))).thenThrow(new IllegalStateException("db down"));

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> service.generate(filters, requesterId));
        verify(auditLogPort, org.mockito.Mockito.never()).logReportRequested(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(reportJobProcessor, org.mockito.Mockito.never()).enqueue(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldNotEnqueueWhenAuditFails() {
        UUID requesterId = UUID.randomUUID();
        when(reportJobRepository.save(any(ReportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        org.mockito.Mockito.doThrow(new IllegalStateException("audit down"))
                .when(auditLogPort).logReportRequested(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> service.generate(new ExecutiveReportFilters(null, null, 2026), requesterId));
        verify(reportJobProcessor, org.mockito.Mockito.never()).enqueue(org.mockito.ArgumentMatchers.any());
    }
}
