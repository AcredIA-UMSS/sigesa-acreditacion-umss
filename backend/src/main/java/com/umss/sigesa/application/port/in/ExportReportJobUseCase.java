package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ReportExportJob;
import com.umss.sigesa.domain.model.ReportFormat;

import java.util.UUID;

public interface ExportReportJobUseCase {
    ReportExportJob enqueueJob(UUID userId, UUID programId, ReportFormat format, Integer phaseId);
    void processJobAsync(UUID jobId);
}
