package com.umss.sigesa.adapter.out.report;

import com.umss.sigesa.application.port.out.ExecutiveDashboardQueryPort;
import com.umss.sigesa.application.port.out.ExecutiveDataPort;
import com.umss.sigesa.domain.model.ExecutiveReportFilters;
import com.umss.sigesa.domain.model.ExecutiveReportSnapshot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Activo cuando MOD-DASH (FSD-UC-013) registra {@link ExecutiveDashboardQueryPort}.
 */
@Component
@Primary
@ConditionalOnBean(ExecutiveDashboardQueryPort.class)
public class ExecutiveDataDashboardAdapter implements ExecutiveDataPort {

    private final ExecutiveDashboardQueryPort dashboardQuery;

    public ExecutiveDataDashboardAdapter(ExecutiveDashboardQueryPort dashboardQuery) {
        this.dashboardQuery = dashboardQuery;
    }

    @Override
    public ExecutiveReportSnapshot fetchSnapshot(ExecutiveReportFilters filters) {
        return dashboardQuery.fetchExecutiveSnapshot(filters);
    }
}
