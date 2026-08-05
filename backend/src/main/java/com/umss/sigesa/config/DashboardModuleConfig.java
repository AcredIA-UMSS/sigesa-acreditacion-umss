package com.umss.sigesa.config;

import com.umss.sigesa.application.port.out.DashboardQueryPort;
import com.umss.sigesa.application.port.out.ReportExportJobRepositoryPort;
import com.umss.sigesa.application.port.out.ReportGeneratorPort;
import com.umss.sigesa.application.service.dashboard.DashboardSummaryAggregationService;
import com.umss.sigesa.application.service.report.ReportExportJobService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Registra servicios del dashboard compuesto y exportación de reportes.
 * {@code SigesaApplication} solo escanea {@code adapter} y {@code config},
 * por eso los {@code @Service} de {@code application} se cablean aquí.
 */
@Configuration
public class DashboardModuleConfig {

    @Bean
    DashboardSummaryAggregationService dashboardSummaryAggregationService(DashboardQueryPort queryPort) {
        return new DashboardSummaryAggregationService(queryPort);
    }

    @Bean
    ReportExportJobService reportExportJobService(
            ReportExportJobRepositoryPort repositoryPort,
            DashboardQueryPort queryPort,
            List<ReportGeneratorPort> reportGenerators) {
        return new ReportExportJobService(repositoryPort, queryPort, reportGenerators);
    }
}
