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
}
