package com.umss.sigesa.domain.model;

import java.util.List;

public record CompositeDashboardSummary(
        String userId,
        List<String> grantedPermissions,
        CoordinatorKpiSection coordinatorSection,
        TechnicianKpiSection technicianSection,
        ExecutiveKpiSection executiveSection
) {}
