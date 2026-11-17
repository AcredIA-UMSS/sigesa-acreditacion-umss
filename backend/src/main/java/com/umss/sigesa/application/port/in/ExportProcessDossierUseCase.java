package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.ReportExportJob;
import com.umss.sigesa.domain.model.ReportFormat;

import java.util.UUID;

public interface ExportProcessDossierUseCase {
    ReportExportJob enqueueJob(UUID userId, UUID processId);
    void processJobAsync(UUID jobId);
}
