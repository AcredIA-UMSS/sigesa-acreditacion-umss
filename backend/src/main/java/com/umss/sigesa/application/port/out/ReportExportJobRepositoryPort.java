package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.ReportExportJob;

import java.util.Optional;
import java.util.UUID;

public interface ReportExportJobRepositoryPort {
    ReportExportJob save(ReportExportJob job);
    Optional<ReportExportJob> findById(UUID jobId);
}
