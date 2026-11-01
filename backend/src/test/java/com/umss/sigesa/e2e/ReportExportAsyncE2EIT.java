package com.umss.sigesa.e2e;

import com.umss.sigesa.application.port.in.ExportReportJobUseCase;
import com.umss.sigesa.application.port.in.GetExportReportJobStatusUseCase;
import com.umss.sigesa.domain.model.JobStatus;
import com.umss.sigesa.domain.model.ReportExportJob;
import com.umss.sigesa.domain.model.ReportFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("ReportExportAsyncE2EIT — Integration E2E Test")
class ReportExportAsyncE2EIT {

    @Autowired
    private ExportReportJobUseCase exportReportJobUseCase;

    @Autowired
    private GetExportReportJobStatusUseCase getExportReportJobStatusUseCase;

    @Test
    @DisplayName("Flujo completo: Encolar -> Procesar -> Verificar Estado persistido")
    void flujoCompletoExportacion() throws InterruptedException {
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();

        ReportExportJob job = exportReportJobUseCase.enqueueJob(userId, programId, ReportFormat.CSV, 1);
        assertNotNull(job);
        assertNotNull(job.getJobId());

        exportReportJobUseCase.processJobAsync(job.getJobId());

        ReportExportJob fetched = waitForTerminalStatus(job.getJobId(), userId);
        assertNotNull(fetched);
        assertEquals(userId, fetched.getUserId());
        assertTrue(fetched.getStatus() == JobStatus.COMPLETED || fetched.getStatus() == JobStatus.FAILED,
                "El job debe persistir estado terminal tras generar el archivo fuera de TX");
    }

    private ReportExportJob waitForTerminalStatus(UUID jobId, UUID userId) throws InterruptedException {
        ReportExportJob fetched = getExportReportJobStatusUseCase.getJobStatus(jobId, userId);
        for (int i = 0; i < 50; i++) {
            if (fetched.getStatus() == JobStatus.COMPLETED || fetched.getStatus() == JobStatus.FAILED) {
                return fetched;
            }
            Thread.sleep(100);
            fetched = getExportReportJobStatusUseCase.getJobStatus(jobId, userId);
        }
        return fetched;
    }
}
