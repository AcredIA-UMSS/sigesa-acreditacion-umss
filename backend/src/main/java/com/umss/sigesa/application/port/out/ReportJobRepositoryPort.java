package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.ReportJob;

import java.util.Optional;
import java.util.UUID;

public interface ReportJobRepositoryPort {

    ReportJob save(ReportJob job);

    Optional<ReportJob> findById(UUID id);
}
