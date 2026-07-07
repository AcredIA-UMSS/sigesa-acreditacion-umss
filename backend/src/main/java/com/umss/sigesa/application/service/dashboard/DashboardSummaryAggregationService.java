package com.umss.sigesa.application.service.dashboard;

import com.umss.sigesa.application.port.in.GetCompositeDashboardSummaryUseCase;
import com.umss.sigesa.application.port.in.GetCoordinatorObservationsDetailsUseCase;
import com.umss.sigesa.application.port.out.DashboardQueryPort;
import com.umss.sigesa.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DashboardSummaryAggregationService implements GetCompositeDashboardSummaryUseCase, GetCoordinatorObservationsDetailsUseCase {

    private final DashboardQueryPort queryPort;

    public DashboardSummaryAggregationService(DashboardQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public CompositeDashboardSummary getSummaryForUser(UUID userId, List<String> permissions, List<UUID> programScopes) {
        CoordinatorKpiSection coordinatorSection = null;
        TechnicianKpiSection technicianSection = null;
        ExecutiveKpiSection executiveSection = null;

        boolean hasCc = permissions.contains("READ_CC_DASHBOARD") || permissions.contains("ROLE_CC") || permissions.contains("CC");
        boolean hasTd = permissions.contains("READ_TD_DASHBOARD") || permissions.contains("ROLE_TD") || permissions.contains("TD");
        boolean hasJd = permissions.contains("READ_JD_DASHBOARD") || permissions.contains("ROLE_JD") || permissions.contains("JD");

        if (hasCc && programScopes != null && !programScopes.isEmpty()) {
            coordinatorSection = queryPort.findCoordinatorKpi(programScopes.get(0));
        }

        if (hasTd) {
            technicianSection = queryPort.findTechnicianKpi(userId);
        }

        if (hasJd) {
            executiveSection = queryPort.findExecutiveKpi();
        }

        return new CompositeDashboardSummary(
                userId.toString(),
                permissions,
                coordinatorSection,
                technicianSection,
                executiveSection
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ObservationSummary> getObservationsDetails(UUID programId, Integer phaseId, String status, Pageable pageable) {
        return queryPort.findObservationDetails(programId, phaseId, status, pageable);
    }
}
