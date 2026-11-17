package com.umss.sigesa.application.port.out;

import com.umss.sigesa.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface DashboardQueryPort {
    CoordinatorKpiSection findCoordinatorKpi(UUID programId);
    TechnicianKpiSection findTechnicianKpi(UUID userId);
    ExecutiveKpiSection findExecutiveKpi();
    Page<ObservationSummary> findObservationDetails(UUID programId, Integer phaseId, String status, Pageable pageable);
    List<ObservationSummary> findObservationsByProgram(UUID programId);
    Stream<ObservationSummary> streamAllObservationsForReport(UUID programId, Integer phaseId);
}
