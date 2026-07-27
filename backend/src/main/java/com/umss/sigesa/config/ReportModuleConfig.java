package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.DownloadReportArtifactUseCase;
import com.umss.sigesa.application.port.in.GenerateExecutiveReportUseCase;
import com.umss.sigesa.application.port.in.GetReportJobStatusUseCase;
import com.umss.sigesa.application.port.in.ProcessReportJobUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.ExecutiveDataPort;
import com.umss.sigesa.application.port.out.PdfRendererPort;
import com.umss.sigesa.application.port.out.ReportArtifactStoragePort;
import com.umss.sigesa.application.port.out.ReportJobRepositoryPort;
import com.umss.sigesa.application.service.report.DownloadReportArtifactService;
import com.umss.sigesa.application.service.report.GenerateExecutiveReportService;
import com.umss.sigesa.application.service.report.GetReportJobStatusService;
import com.umss.sigesa.application.service.report.ProcessReportJobService;
import com.umss.sigesa.application.service.report.ReportJobProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportModuleConfig {

    @Bean
    GenerateExecutiveReportUseCase generateExecutiveReportUseCase(
            ReportJobRepositoryPort reportJobRepository,
            AuditLogPort auditLogPort,
            ReportJobProcessor reportJobProcessor) {
        return new GenerateExecutiveReportService(reportJobRepository, auditLogPort, reportJobProcessor);
    }

    @Bean
    GetReportJobStatusUseCase getReportJobStatusUseCase(ReportJobRepositoryPort reportJobRepository) {
        return new GetReportJobStatusService(reportJobRepository);
    }

    @Bean
    ProcessReportJobUseCase processReportJobUseCase(
            ReportJobRepositoryPort reportJobRepository,
            ExecutiveDataPort executiveDataPort,
            PdfRendererPort pdfRendererPort,
            ReportArtifactStoragePort artifactStorage) {
        return new ProcessReportJobService(
                reportJobRepository, executiveDataPort, pdfRendererPort, artifactStorage);
    }

    @Bean
    DownloadReportArtifactUseCase downloadReportArtifactUseCase(
            ReportJobRepositoryPort reportJobRepository,
            ReportArtifactStoragePort artifactStorage) {
        return new DownloadReportArtifactService(reportJobRepository, artifactStorage);
    }
}
