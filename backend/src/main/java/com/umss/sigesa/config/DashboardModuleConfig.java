package com.umss.sigesa.config;

import com.umss.sigesa.application.port.out.DashboardQueryPort;
import com.umss.sigesa.application.service.dashboard.DashboardSummaryAggregationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DashboardModuleConfig {

    @Bean
    DashboardSummaryAggregationService dashboardSummaryAggregationService(
            DashboardQueryPort queryPort) {
        return new DashboardSummaryAggregationService(queryPort);
    }

    @Bean
    com.umss.sigesa.application.port.in.GetTechnicianInboxUseCase getTechnicianInboxUseCase(
            com.umss.sigesa.application.port.out.TechnicianInboxQueryPort queryPort) {
        return new com.umss.sigesa.application.service.dashboard.GetTechnicianInboxService(queryPort);
    }
}
