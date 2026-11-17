package com.umss.sigesa.application.service.dashboard;

import com.umss.sigesa.application.port.out.DashboardQueryPort;
import com.umss.sigesa.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardSummaryAggregationService — PBAC Summary Test")
class DashboardSummaryAggregationServiceTest {

    @Mock
    private DashboardQueryPort queryPort;

    private DashboardSummaryAggregationService service;

    @BeforeEach
    void setUp() {
        service = new DashboardSummaryAggregationService(queryPort);
    }

    @Test
    @DisplayName("User with CC permission only receives coordinatorSection")
    void summarySoloCc_retornaSoloCoordinator() {
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        CoordinatorKpiSection kpi = new CoordinatorKpiSection(programId, "Systems", 10, 50.0, 5, 2, 3, List.of(), List.of());

        when(queryPort.findCoordinatorKpi(programId)).thenReturn(kpi);

        CompositeDashboardSummary summary = service.getSummaryForUser(userId, List.of("READ_CC_DASHBOARD"), List.of(programId));

        assertNotNull(summary);
        assertNotNull(summary.coordinatorSection());
        assertNull(summary.technicianSection());
        assertNull(summary.executiveSection());
    }

    @Test
    @DisplayName("Multi-role user (CC and TD) receives both sections")
    void summaryMultiRol_retornaMultiplesSecciones() {
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        CoordinatorKpiSection ccKpi = new CoordinatorKpiSection(programId, "Systems", 10, 50.0, 5, 2, 3, List.of(), List.of());
        TechnicianKpiSection tdKpi = new TechnicianKpiSection(4, 8, 2, 6, List.of());

        when(queryPort.findCoordinatorKpi(programId)).thenReturn(ccKpi);
        when(queryPort.findTechnicianKpi(userId)).thenReturn(tdKpi);

        CompositeDashboardSummary summary = service.getSummaryForUser(userId, List.of("READ_CC_DASHBOARD", "READ_TD_DASHBOARD"), List.of(programId));

        assertNotNull(summary);
        assertNotNull(summary.coordinatorSection());
        assertNotNull(summary.technicianSection());
        assertNull(summary.executiveSection());
    }

    @Test
    @DisplayName("User with JD permission receives executiveSection")
    void summaryJd_retornaExecutiveSection() {
        UUID userId = UUID.randomUUID();
        ExecutiveKpiSection jdKpi = new ExecutiveKpiSection(5, 75.0, 10, 2, List.of());

        when(queryPort.findExecutiveKpi()).thenReturn(jdKpi);

        CompositeDashboardSummary summary = service.getSummaryForUser(userId, List.of("READ_JD_DASHBOARD"), List.of());

        assertNotNull(summary);
        assertNull(summary.coordinatorSection());
        assertNull(summary.technicianSection());
        assertNotNull(summary.executiveSection());
    }

    @Test
    @DisplayName("getObservationsDetails: Delegates correctly to query port")
    void getObservationsDetails_delegaAlPuerto() {
        UUID programId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 10);
        when(queryPort.findObservationDetails(programId, 1, "PENDING", pageable))
                .thenReturn(new PageImpl<>(List.of()));

        Page<ObservationSummary> result = service.getObservationsDetails(programId, 1, "PENDING", pageable);
        assertNotNull(result);
        verify(queryPort).findObservationDetails(programId, 1, "PENDING", pageable);
    }

    @Test
    void shouldReturnEmptySectionsWhenUserHasNoPermissions() {
        UUID userId = UUID.randomUUID();

        CompositeDashboardSummary summary = service.getSummaryForUser(userId, List.of(), List.of(UUID.randomUUID()));

        assertNull(summary.coordinatorSection());
        assertNull(summary.technicianSection());
        assertNull(summary.executiveSection());
        verify(queryPort, never()).findCoordinatorKpi(any());
        verify(queryPort, never()).findTechnicianKpi(any());
        verify(queryPort, never()).findExecutiveKpi();
    }

    @Test
    void shouldSkipCoordinatorSectionWhenProgramScopesAreEmpty() {
        UUID userId = UUID.randomUUID();

        CompositeDashboardSummary summary = service.getSummaryForUser(
                userId, List.of("READ_CC_DASHBOARD"), List.of());

        assertNull(summary.coordinatorSection());
        verify(queryPort, never()).findCoordinatorKpi(any());
    }

    @Test
    void shouldLoadCoordinatorSectionForEePermission() {
        UUID userId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        CoordinatorKpiSection kpi = new CoordinatorKpiSection(programId, "Civil", 0, 0.0, 0, 0, 0, List.of(), List.of());
        when(queryPort.findCoordinatorKpi(programId)).thenReturn(kpi);

        CompositeDashboardSummary summary = service.getSummaryForUser(userId, List.of("EE"), List.of(programId));

        assertNotNull(summary.coordinatorSection());
        assertNull(summary.technicianSection());
        verify(queryPort).findCoordinatorKpi(programId);
        verify(queryPort, never()).findTechnicianKpi(any());
    }

    @Test
    void shouldReturnEmptyObservationsPage() {
        UUID programId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 10);
        when(queryPort.findObservationDetails(programId, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        Page<ObservationSummary> result = service.getObservationsDetails(programId, null, null, pageable);

        assertTrue(result.isEmpty());
        verify(queryPort).findObservationDetails(programId, null, null, pageable);
    }
}
