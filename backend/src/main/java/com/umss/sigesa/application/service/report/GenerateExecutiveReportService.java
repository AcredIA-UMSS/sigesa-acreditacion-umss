package com.umss.sigesa.application.service.report;

import com.umss.sigesa.application.port.in.GenerateExecutiveReportUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.ReportJobRepositoryPort;
import com.umss.sigesa.domain.model.ExecutiveReportFilters;
import com.umss.sigesa.domain.model.ReportJob;

import java.util.UUID;

public class GenerateExecutiveReportService implements GenerateExecutiveReportUseCase {

    private final ReportJobRepositoryPort reportJobRepository;
    private final AuditLogPort auditLogPort;
    private final ReportJobProcessor reportJobProcessor;

    public GenerateExecutiveReportService(ReportJobRepositoryPort reportJobRepository,
                                          AuditLogPort auditLogPort,
                                          ReportJobProcessor reportJobProcessor) {
        this.reportJobRepository = reportJobRepository;
        this.auditLogPort = auditLogPort;
        this.reportJobProcessor = reportJobProcessor;
    }

    @Override
    public UUID generate(ExecutiveReportFilters filters, UUID requesterId) {
        ReportJob job = ReportJob.createPending(UUID.randomUUID(), requesterId, filters);
        reportJobRepository.save(job);
        auditLogPort.logReportRequested(requesterId, job.getId());
        reportJobProcessor.enqueue(job.getId());
        return job.getId();
    }
}
