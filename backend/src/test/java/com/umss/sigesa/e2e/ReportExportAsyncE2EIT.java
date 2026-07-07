package com.umss.sigesa.e2e;

import com.umss.sigesa.application.port.in.ExportReportJobUseCase;
import com.umss.sigesa.application.port.in.GetReportJobStatusUseCase;
import com.umss.sigesa.domain.model.ReportExportJob;
import com.umss.sigesa.domain.model.ReportFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("ReportExportAsyncE2EIT — Integration E2E Test")
class ReportExportAsyncE2EIT {

    @Autowired
    private ExportReportJobUseCase exportReportJobUseCase;

    @Autowired
    private GetReportJobStatusUseCase getReportJobStatusUseCase;

    @Test
    @DisplayName("Flujo completo: Encolar -> Procesar -> Verificar Estado")
    void flujoCompletoExportacion() {
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();

        ReportExportJob job = exportReportJobUseCase.enqueueJob(userId, programId, ReportFormat.CSV, 1);
        assertNotNull(job);
        assertNotNull(job.getJobId());

        exportReportJobUseCase.processJobAsync(job.getJobId());

        ReportExportJob fetched = getReportJobStatusUseCase.getJobStatus(job.getJobId(), userId);
        assertNotNull(fetched);
        assertEquals(userId, fetched.getUserId());
    }
}
