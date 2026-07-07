package com.umss.sigesa.application.port.in;

import com.umss.sigesa.domain.model.CompositeDashboardSummary;

import java.util.List;
import java.util.UUID;

public interface GetCompositeDashboardSummaryUseCase {
    CompositeDashboardSummary getSummaryForUser(UUID userId, List<String> permissions, List<UUID> programScopes);
}
