package com.umss.sigesa.application.service;

import com.umss.sigesa.application.port.out.DashboardQueryPort;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.application.port.out.IndicatorCatalogPort;
import com.umss.sigesa.application.port.out.IndicatorStateHistoryPort;
import com.umss.sigesa.application.port.out.ObservationRepositoryPort;
import com.umss.sigesa.domain.model.AuthenticatedIdentity;
import com.umss.sigesa.domain.model.Email;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Indicator Workflow Services — Approve & Reject Tests")
class IndicatorWorkflowServiceTest {

    @Mock
    private IndicatorCatalogPort indicatorCatalog;
    @Mock
    private IndicatorStateHistoryPort indicatorStateHistory;
    @Mock
    private ObservationRepositoryPort observationRepository;
    @Mock
    private DashboardQueryPort dashboardQueryPort;
    @Mock
    private EvidenceRepositoryPort evidenceRepository;

    private ApproveIndicatorService approveService;
    private RejectIndicatorService rejectService;

    private UUID indicatorId;
    private UUID programId;
    private AuthenticatedIdentity tdIdentity;

    @BeforeEach
    void setUp() {
        approveService = new ApproveIndicatorService(indicatorCatalog, indicatorStateHistory, observationRepository, dashboardQueryPort);
        rejectService = new RejectIndicatorService(indicatorCatalog, indicatorStateHistory, observationRepository, dashboardQueryPort, evidenceRepository);
        indicatorId = UUID.randomUUID();
        programId = UUID.randomUUID();

        tdIdentity = new AuthenticatedIdentity(
                UUID.randomUUID(),
                Email.of("td@umss.edu.bo"),
                Role.TD,
                List.of(programId)
        );
    }

    @Test
    @DisplayName("Approve Indicator — Updates Dashboard Summary and Resolves Observations")
    void approve_successful() {
        IndicatorCatalogPort.IndicatorEntry entry = new IndicatorCatalogPort.IndicatorEntry(
                indicatorId, "IND-101", "Title", programId, 1, UUID.randomUUID()
        );
        when(indicatorCatalog.findById(indicatorId)).thenReturn(Optional.of(entry));
        when(indicatorStateHistory.findLatestState(indicatorId)).thenReturn(Optional.of("SUBIDO"));
        when(indicatorStateHistory.findLatestHistoryId(indicatorId)).thenReturn(Optional.of(UUID.randomUUID()));
        when(observationRepository.resolveObservationForIndicator(programId, indicatorId.toString(), "APROBADO")).thenReturn(1);

        var result = approveService.approve(indicatorId, tdIdentity);

        assertNotNull(result);
        assertEquals("APROBADO", result.newState());

        verify(observationRepository).resolveObservationForIndicator(programId, indicatorId.toString(), "APROBADO");
        verify(dashboardQueryPort).updateDashboardMetrics(programId, 1, 0, -1);
        verify(indicatorStateHistory).recordTransition(eq(indicatorId), eq("SUBIDO"), eq("APROBADO"), any(), eq(Role.TD));
    }

    @Test
    @DisplayName("Reject Indicator — Saves Pending Observation and Updates Dashboard Metrics")
    void reject_successful() {
        IndicatorCatalogPort.IndicatorEntry entry = new IndicatorCatalogPort.IndicatorEntry(
                indicatorId, "IND-101", "Title", programId, 1, UUID.randomUUID()
        );
        when(indicatorCatalog.findById(indicatorId)).thenReturn(Optional.of(entry));
        when(indicatorStateHistory.findLatestState(indicatorId)).thenReturn(Optional.of("SUBIDO"));
        when(indicatorStateHistory.findLatestHistoryId(indicatorId)).thenReturn(Optional.of(UUID.randomUUID()));

        UUID latestVersionId = UUID.randomUUID();
        Evidence evidence = new Evidence(UUID.randomUUID(), indicatorId, latestVersionId, java.time.LocalDateTime.now());
        when(evidenceRepository.findByIndicatorId(indicatorId)).thenReturn(Optional.of(evidence));

        var result = rejectService.reject(indicatorId, "La justificación debe tener al menos 20 caracteres de longitud para ser válida.", tdIdentity);

        assertNotNull(result);
        assertEquals("OBSERVADO", result.newState());

        verify(observationRepository).savePendingObservation(any());
        verify(dashboardQueryPort).updateDashboardMetrics(programId, 0, 1, 1);
        verify(indicatorStateHistory).recordTransition(eq(indicatorId), eq("SUBIDO"), eq("OBSERVADO"), any(), eq(Role.TD));
    }
}
